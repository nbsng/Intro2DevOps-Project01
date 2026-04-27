pipeline {
    agent any

    tools {
        jdk 'JDK 25' 
        maven 'Maven'
        nodejs 'Node 20'
        snyk 'snyk'
    }

    environment {
        SNYK_HOME = tool name: 'snyk' 
        PATH = "${SNYK_HOME}:${env.PATH}"
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {

        // ==========================================
        // 0. GITLEAKS - QUÉT LỘ MẬT KHẨU/TOKEN TOÀN DỰ ÁN
        // ==========================================
        stage('Security: Gitleaks Scan') {
            steps {
                echo "[INFO] Đang quét mã nguồn để tìm mật khẩu, token bị lộ (Gitleaks)..."
                sh 'gitleaks detect --source . --verbose --report-path gitleaks-report.json || true'
                archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
            }
        }

        // ==========================================
        // 1. BACKOFFICE (Node.js)
        // ==========================================
        stage('CI: Backoffice') {
            when { changeset "backoffice/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('backoffice') {
                            echo "[INFO] Đang cài đặt thư viện và build Backoffice..."
                            sh 'npm ci'
                            sh 'npm run build'
                        }
                    }
                }
                stage('Format Check') {
                    steps {
                        dir('backoffice') {
                            echo "[INFO] Đang kiểm tra Format..."
                            sh 'npm run lint'
                            sh 'npx prettier --check .'
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        dir('backoffice') {
                            echo "[INFO] Quét SonarQube..."
                            withSonarQubeEnv('Sonar-Server') {
                                sh 'sonar-scanner -Dsonar.projectKey=backoffice -Dsonar.sources=.'
                            }
                            echo "[INFO] Quét Snyk..."
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh 'snyk test --all-projects --json-file-output=snyk-backoffice-report.json || true'
                            }
                            archiveArtifacts artifacts: 'snyk-backoffice-report.json', allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. BACKOFFICE-BFF (Maven)
        // ==========================================
        stage('CI: Backoffice-bff') {
            when { changeset "backoffice-bff/**" }
            stages {
                stage('Verify & Checkstyle') {
                    steps {
                        echo "[INFO] Đang chạy Verify và Checkstyle cho Backoffice-bff..."
                        sh 'mvn clean verify checkstyle:checkstyle -DskipTests -pl backoffice-bff -am'
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl backoffice-bff -am -Dsonar.projectKey=backoffice-bff'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-backoffice-bff-report.json --target-dir=backoffice-bff || true'
                        }
                        archiveArtifacts artifacts: 'snyk-backoffice-bff-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 3. CART (Maven Core)
        // ==========================================
        stage('CI: Cart') {
            when { changeset "cart/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl cart -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl cart -am'
                    }
                    post {
                        always {
                            junit testResults: 'cart/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'cart/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl cart -am -Dsonar.projectKey=cart'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-cart-report.json --target-dir=cart || true'
                        }
                        archiveArtifacts artifacts: 'snyk-cart-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 4. CUSTOMER (Maven Core)
        // ==========================================
        stage('CI: Customer') {
            when { changeset "customer/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl customer -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl customer -am'
                    }
                    post {
                        always {
                            junit testResults: 'customer/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'customer/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl customer -am -Dsonar.projectKey=customer'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-customer-report.json --target-dir=customer || true'
                        }
                        archiveArtifacts artifacts: 'snyk-customer-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 5. INVENTORY (Maven Core)
        // ==========================================
        stage('CI: Inventory') {
            when { changeset "inventory/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl inventory -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl inventory -am'
                    }
                    post {
                        always {
                            junit testResults: 'inventory/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'inventory/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl inventory -am -Dsonar.projectKey=inventory'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-inventory-report.json --target-dir=inventory || true'
                        }
                        archiveArtifacts artifacts: 'snyk-inventory-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 6. LOCATION (Maven Core)
        // ==========================================
        stage('CI: Location') {
            when { changeset "location/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl location -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl location -am'
                    }
                    post {
                        always {
                            junit testResults: 'location/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'location/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl location -am -Dsonar.projectKey=location'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-location-report.json --target-dir=location || true'
                        }
                        archiveArtifacts artifacts: 'snyk-location-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 7. MEDIA (Maven Core)
        // ==========================================
        stage('CI: Media') {
            when { changeset "media/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl media -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl media -am'
                    }
                    post {
                        always {
                            junit testResults: 'media/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'media/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl media -am -Dsonar.projectKey=media'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-media-report.json --target-dir=media || true'
                        }
                        archiveArtifacts artifacts: 'snyk-media-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 8. ORDER (Maven Core)
        // ==========================================
        stage('CI: Order') {
            when { changeset "order/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl order -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl order -am'
                    }
                    post {
                        always {
                            junit testResults: 'order/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'order/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl order -am -Dsonar.projectKey=order'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-order-report.json --target-dir=order || true'
                        }
                        archiveArtifacts artifacts: 'snyk-order-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 9. PAYMENT (Maven Core)
        // ==========================================
        stage('CI: Payment') {
            when { changeset "payment/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl payment -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl payment -am'
                    }
                    post {
                        always {
                            junit testResults: 'payment/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'payment/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl payment -am -Dsonar.projectKey=payment'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-payment-report.json --target-dir=payment || true'
                        }
                        archiveArtifacts artifacts: 'snyk-payment-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 10. PAYMENT-PAYPAL (Maven Core)
        // ==========================================
        stage('CI: Payment Paypal') {
            when { changeset "payment-paypal/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl payment-paypal -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl payment-paypal -am'
                    }
                    post {
                        always {
                            junit testResults: 'payment-paypal/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'payment-paypal/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl payment-paypal -am -Dsonar.projectKey=payment-paypal'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-payment-paypal-report.json --target-dir=payment-paypal || true'
                        }
                        archiveArtifacts artifacts: 'snyk-payment-paypal-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 11. PRODUCT (Maven Core)
        // ==========================================
        stage('CI: Product') {
            when { changeset "product/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl product -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl product -am'
                    }
                    post {
                        always {
                            junit testResults: 'product/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'product/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl product -am -Dsonar.projectKey=product'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-product-report.json --target-dir=product || true'
                        }
                        archiveArtifacts artifacts: 'snyk-product-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 12. PROMOTION (Maven Core)
        // ==========================================
        stage('CI: Promotion') {
            when { changeset "promotion/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl promotion -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl promotion -am'
                    }
                    post {
                        always {
                            junit testResults: 'promotion/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'promotion/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl promotion -am -Dsonar.projectKey=promotion'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-promotion-report.json --target-dir=promotion || true'
                        }
                        archiveArtifacts artifacts: 'snyk-promotion-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 13. RATING (Maven Core)
        // ==========================================
        stage('CI: Rating') {
            when { changeset "rating/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl rating -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl rating -am'
                    }
                    post {
                        always {
                            junit testResults: 'rating/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'rating/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl rating -am -Dsonar.projectKey=rating'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-rating-report.json --target-dir=rating || true'
                        }
                        archiveArtifacts artifacts: 'snyk-rating-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 14. RECOMMENDATION (Maven Core)
        // ==========================================
        stage('CI: Recommendation') {
            when { changeset "recommendation/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl recommendation -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl recommendation -am'
                    }
                    post {
                        always {
                            junit testResults: 'recommendation/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'recommendation/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl recommendation -am -Dsonar.projectKey=recommendation'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-recommendation-report.json --target-dir=recommendation || true'
                        }
                        archiveArtifacts artifacts: 'snyk-recommendation-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 15. SAMPLE DATA (Maven Core)
        // ==========================================
        stage('CI: Sample data') {
            when { changeset "sampledata/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl sampledata -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl sampledata -am'
                    }
                    post {
                        always {
                            junit testResults: 'sampledata/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'sampledata/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl sampledata -am -Dsonar.projectKey=sampledata'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-sampledata-report.json --target-dir=sampledata || true'
                        }
                        archiveArtifacts artifacts: 'snyk-sampledata-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 16. SEARCH (Maven Core)
        // ==========================================
        stage('CI: Search') {
            when { changeset "search/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl search -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl search -am'
                    }
                    post {
                        always {
                            junit testResults: 'search/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'search/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl search -am -Dsonar.projectKey=search'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-search-report.json --target-dir=search || true'
                        }
                        archiveArtifacts artifacts: 'snyk-search-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 17. STOREFRONT-BFF (Maven)
        // ==========================================
        stage('CI: Storefront-bff') {
            when { changeset "storefront-bff/**" }
            stages {
                stage('Verify & Checkstyle') {
                    steps {
                        echo "[INFO] Đang chạy Verify và Checkstyle cho Storefront-bff..."
                        sh 'mvn clean verify checkstyle:checkstyle -DskipTests -pl storefront-bff -am'
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl storefront-bff -am -Dsonar.projectKey=storefront-bff'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-storefront-bff-report.json --target-dir=storefront-bff || true'
                        }
                        archiveArtifacts artifacts: 'snyk-storefront-bff-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 18. STOREFRONT (Node.js)
        // ==========================================
        stage('CI: Storefront') {
            when { changeset "storefront/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('storefront') {
                            echo "[INFO] Đang cài đặt thư viện và build Storefront..."
                            sh 'npm ci'
                            sh 'npm run build'
                        }
                    }
                }
                stage('Format Check') {
                    steps {
                        dir('storefront') {
                            echo "[INFO] Đang kiểm tra Format..."
                            sh 'npm run lint'
                            sh 'npx prettier --check .'
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        dir('storefront') {
                            echo "[INFO] Quét SonarQube..."
                            withSonarQubeEnv('Sonar-Server') {
                                sh 'sonar-scanner -Dsonar.projectKey=storefront -Dsonar.sources=.'
                            }
                            echo "[INFO] Quét Snyk..."
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh 'snyk test --all-projects --json-file-output=snyk-storefront-report.json || true'
                            }
                            archiveArtifacts artifacts: 'snyk-storefront-report.json', allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        // ==========================================
        // 19. TAX (Maven Core)
        // ==========================================
        stage('CI: Tax') {
            when { changeset "tax/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl tax -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl tax -am'
                    }
                    post {
                        always {
                            junit testResults: 'tax/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'tax/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl tax -am -Dsonar.projectKey=tax'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-tax-report.json --target-dir=tax || true'
                        }
                        archiveArtifacts artifacts: 'snyk-tax-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 20. WEBHOOK (Maven Core)
        // ==========================================
        stage('CI: Webhook') {
            when { changeset "webhook/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl webhook -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl webhook -am'
                    }
                    post {
                        always {
                            junit testResults: 'webhook/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'webhook/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl webhook -am -Dsonar.projectKey=webhook'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-webhook-report.json --target-dir=webhook || true'
                        }
                        archiveArtifacts artifacts: 'snyk-webhook-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 21. COMMON-LIBRARY (Maven Core)
        // ==========================================
        stage('CI: Common-library') {
            when { changeset "common-library/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl common-library -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        sh 'mvn test jacoco:report -pl common-library -am'
                    }
                    post {
                        always {
                            junit testResults: 'common-library/target/surefire-reports/*.xml', allowEmptyResults: true
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: 'common-library/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', unstable: false],
                                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                ]
                            )
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl common-library -am -Dsonar.projectKey=common-library'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-common-library-report.json --target-dir=common-library || true'
                        }
                        archiveArtifacts artifacts: 'snyk-common-library-report.json', allowEmptyArchive: true
                    }
                }
            }
        }

        // ==========================================
        // 22. DELIVERY (Maven Core)
        // ==========================================
        stage('CI: Delivery') {
            when { changeset "delivery/**" }
            stages {
                stage('Build') {
                    steps {
                        sh 'mvn clean install -DskipTests -pl delivery -am'
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        script {
                            if (fileExists('delivery/src/test')) {
                                echo "[INFO] Phát hiện thư mục test trong Delivery, đang chạy Test..."
                                sh 'mvn test jacoco:report -pl delivery -am'
                            } else {
                                echo "[INFO] Không tìm thấy thư mục test trong Delivery, bỏ qua."
                            }
                        }
                    }
                    post {
                        always {
                            script {
                                if (fileExists('delivery/target/site/jacoco/jacoco.xml')) {
                                    junit testResults: 'delivery/target/surefire-reports/*.xml', allowEmptyResults: true
                                    recordCoverage(
                                        tools: [[parser: 'JACOCO', pattern: 'delivery/target/site/jacoco/jacoco.xml']],
                                        qualityGates: [
                                            [threshold: 70.0, metric: 'LINE', unstable: false],
                                            [threshold: 70.0, metric: 'BRANCH', unstable: false]
                                        ]
                                    )
                                }
                            }
                        }
                    }
                }
                stage('Security & Quality (Sonar, Snyk)') {
                    steps {
                        echo "[INFO] Quét SonarQube..."
                        withSonarQubeEnv('Sonar-Server') {
                            sh 'mvn sonar:sonar -pl delivery -am -Dsonar.projectKey=delivery'
                        }
                        echo "[INFO] Quét Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'snyk test --all-projects --json-file-output=snyk-delivery-report.json --target-dir=delivery || true'
                        }
                        archiveArtifacts artifacts: 'snyk-delivery-report.json', allowEmptyArchive: true
                    }
                }
            }
        }
    }

    // ==========================================
    // DỌN DẸP & THÔNG BÁO SAU KHI CHẠY
    // ==========================================
    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Pipeline hoàn thành thành công!"
        }
        failure {
            echo "❌ Pipeline thất bại!"
        }
    }
}