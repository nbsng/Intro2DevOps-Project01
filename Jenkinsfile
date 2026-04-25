pipeline {
    agent any

    tools {
        jdk 'JDK 25' 
        maven 'Maven'
        nodejs 'Node 20'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {

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
                        // Bỏ dir(), chạy từ root với -pl backoffice-bff
                        echo "[INFO] Đang chạy Verify và Checkstyle cho Backoffice-bff..."
                        sh 'mvn clean verify checkstyle:checkstyle -DskipTests -pl backoffice-bff -am'
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
                        sh 'mvn test jacoco:report -pl delivery -am'
                    }
                    post {
                        always {
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

        // ==========================================
        // 23. QUALITY & SECURITY (SONARQUBE + SNYK)
        // ==========================================
        stage('Quality & Security Scan') {
            // when {
            //     anyOf {
            //         changeset "backoffice/**"
            //         changeset "backoffice-bff/**"
            //         changeset "cart/**"
            //         changeset "common-library/**"
            //         changeset "customer/**"
            //         changeset "delivery/**"
            //         changeset "inventory/**"
            //         changeset "location/**"
            //         changeset "media/**"
            //         changeset "order/**"
            //         changeset "payment/**"
            //         changeset "payment-paypal/**"
            //         changeset "product/**"
            //         changeset "promotion/**"
            //         changeset "rating/**"
            //         changeset "recommendation/**"
            //         changeset "sampledata/**"
            //         changeset "search/**"
            //         changeset "storefront/**"
            //         changeset "storefront-bff/**"
            //         changeset "tax/**"
            //         changeset "webhook/**"
            //         changeset "pom.xml"
            //     }
            // }
            stages {
                stage('SonarQube Analysis') {
                    steps {
                        echo "[INFO] Đang chạy phân tích SonarQube..."
                        withSonarQubeEnv('SonarQubeServer') {
                            sh 'mvn clean verify sonar:sonar -DskipTests'
                        }
                    }
                }
                stage('Snyk OSS Scan') {
                    steps {
                        echo "[INFO] Đang chạy quét lỗ hổng Snyk..."
                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            sh 'npx snyk auth $SNYK_TOKEN'
                            sh 'npx snyk test --all-projects --detection-depth=4 --severity-threshold=high --json-file-output=snyk-report.json || true'
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'snyk-report.json', allowEmptyArchive: true
                        }
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