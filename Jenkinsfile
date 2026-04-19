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
        // 1. BACKOFFICE
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
        // 2. BACKOFFICE-BFF
        // ==========================================
        stage('CI: Backoffice-bff') {
            when { changeset "backoffice-bff/**" }
            stages {
                stage('Verify & Checkstyle') {
                    steps {
                        dir('backoffice-bff') {
                            echo "[INFO] Đang chạy Verify và Checkstyle cho Backoffice-bff..."
                            sh 'mvn clean verify checkstyle:checkstyle -DskipTests -pl . -am'
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. CART
        // ==========================================
        stage('CI: Cart') {
            when { changeset "cart/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('cart') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('cart') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
                    }
                    post {
                        always {
                            // Đọc kết quả JUnit Test để vẽ biểu đồ Pass/Fail
                            junit testResults: 'cart/target/surefire-reports/*.xml', allowEmptyResults: true
                            // Kiểm tra Quality Gate: 70% Line và 70% Branch
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
        // 4. CUSTOMER
        // ==========================================
        stage('CI: Customer') {
            when { changeset "customer/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('customer') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('customer') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 5. INVENTORY
        // ==========================================
        stage('CI: Inventory') {
            when { changeset "inventory/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('inventory') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('inventory') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 6. LOCATION
        // ==========================================
        stage('CI: Location') {
            when { changeset "location/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('location') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('location') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 7. MEDIA
        // ==========================================
        stage('CI: Media') {
            when { changeset "media/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('media') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('media') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 8. ORDER
        // ==========================================
        stage('CI: Order') {
            when { changeset "order/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('order') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('order') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 9. PAYMENT
        // ==========================================
        stage('CI: Payment') {
            when { changeset "payment/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('payment') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('payment') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 10. PAYMENT-PAYPAL
        // ==========================================
        stage('CI: Payment Paypal') {
            when { changeset "payment-paypal/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('payment-paypal') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('payment-paypal') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 11. PRODUCT
        // ==========================================
        stage('CI: Product') {
            when { changeset "product/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('product') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('product') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 12. PROMOTION
        // ==========================================
        stage('CI: Promotion') {
            when { changeset "promotion/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('promotion') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('promotion') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 13. RATING
        // ==========================================
        stage('CI: Rating') {
            when { changeset "rating/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('rating') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('rating') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 14. RECOMMENDATION
        // ==========================================
        stage('CI: Recommendation') {
            when { changeset "recommendation/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('recommendation') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('recommendation') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 15. SAMPLE DATA
        // ==========================================
        stage('CI: Sample data') {
            when { changeset "sampledata/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('sampledata') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('sampledata') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 16. SEARCH
        // ==========================================
        stage('CI: Search') {
            when { changeset "search/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('search') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('search') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 17. STOREFRONT-BFF
        // ==========================================
        stage('CI: Storefront-bff') {
            when { changeset "storefront-bff/**" }
            stages {
                stage('Verify & Checkstyle') {
                    steps {
                        dir('storefront-bff') {
                            echo "[INFO] Đang chạy Verify và Checkstyle cho Storefront-bff..."
                            sh 'mvn clean verify checkstyle:checkstyle -DskipTests -pl . -am'
                        }
                    }
                }
            }
        }

        // ==========================================
        // 18. STOREFRONT
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
        // 19. TAX
        // ==========================================
        stage('CI: Tax') {
            when { changeset "tax/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('tax') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('tax') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 20. WEBHOOK
        // ==========================================
        stage('CI: Webhook') {
            when { changeset "webhook/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('webhook') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('webhook') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 20. COMMON-LIBRARY
        // ==========================================
        stage('CI: Common-library') {
            when { changeset "common-library/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('common-library') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('common-library') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
        // 21. DELIVERY
        // ==========================================
        stage('CI: Delivery') {
            when { changeset "delivery/**" }
            stages {
                stage('Build') {
                    steps {
                        dir('delivery') {
                            sh 'mvn clean install -DskipTests -pl . -am'
                        }
                    }
                }
                stage('Test & Coverage') {
                    steps {
                        dir('delivery') {
                            sh 'mvn test jacoco:report -pl . -am'
                        }
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
    }

    // ==========================================
    // DỌN DẸP & THÔNG BÁO SAU KHI CHẠY
    // ==========================================
    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Pipeline hoàn thành thành công!."
        }
        failure {
            echo "❌ Pipeline thất bại!"
        }
    }
}