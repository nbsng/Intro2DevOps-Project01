// Khai báo biến toàn cục để lưu danh sách các thư mục có sự thay đổi
def changedFolders = []

pipeline {
    agent any

    tools {
        jdk 'JDK 25' 
        maven 'Maven'
        nodejs 'Node 20'
        snyk 'snyk'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {
        // ==========================================
        // 0. SYSTEM: TỰ ĐỘNG PHÁT HIỆN SỰ THAY ĐỔI
        // ==========================================
        stage('System: Detect Changes') {
            steps {
                script {
                    echo "[INFO] Đang phân tích sự thay đổi của mã nguồn (Native Git Diff)..."
                    def baseRef = ""
                    
                    if (env.CHANGE_ID) {
                        baseRef = "origin/${env.CHANGE_TARGET}"
                        echo "[INFO] Phát hiện Pull Request. Đang so sánh HEAD với ${baseRef}..."
                    } else if (currentBuild.previousBuild == null) {
                        echo "[INFO] Nhánh mới được tạo (First Build). Đang dò tìm nhánh mẹ gần nhất..."
                        baseRef = sh(script: '''
                            CURRENT_BRANCH=${BRANCH_NAME}
                            git for-each-ref --format='%(refname:short)' refs/remotes/origin/ | grep -v "origin/${CURRENT_BRANCH}" | while read branch; do
                                mb=$(git merge-base HEAD "$branch" 2>/dev/null || true)
                                if [ -n "$mb" ]; then
                                    dist=$(git rev-list --count "$mb"..HEAD)
                                    echo "$dist $mb $branch"
                                fi
                            done | sort -n | head -n 1 | awk '{print $2}'
                        ''', returnStdout: true).trim()
                        
                        if (!baseRef) { baseRef = "HEAD~1" }
                        echo "[INFO] Đã tìm thấy điểm rẽ nhánh gốc: ${baseRef}..."
                    } else {
                        baseRef = env.GIT_PREVIOUS_COMMIT ?: "HEAD~1"
                        echo "[INFO] Push cập nhật nhánh. So sánh HEAD với commit build trước đó (${baseRef})..."
                    }

                    sh "git fetch origin || true"
                    def diffOutput = sh(script: "git diff --name-only ${baseRef}...HEAD || true", returnStdout: true).trim()

                    if (diffOutput) {
                        def files = diffOutput.split('\n')
                        for (int i = 0; i < files.length; i++) {
                            def pathParts = files[i].split('/')
                            if (pathParts.length > 0) { changedFolders.add(pathParts[0]) }
                        }
                    }
                    changedFolders = changedFolders.unique()
                    echo "=> CÁC DỊCH VỤ CÓ SỰ THAY ĐỔI LÀ: ${changedFolders}"
                }
            }
        }

        stage('Security: Gitleaks Scan') {
            steps {
                echo "[INFO] Đang quét mã nguồn để tìm mật khẩu, token bị lộ (Gitleaks)..."
                sh 'gitleaks detect --source . --verbose --report-path gitleaks-report.json || true'
                archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
            }
        }

        // ==========================================
        // CÁC SERVICES NODE.JS
        // ==========================================
        stage('CI: Backoffice') {
            when { expression { return changedFolders.contains('backoffice') } }
            stages {
                stage('Build') { steps { buildNodeService('backoffice') } }
                stage('Format Check') { steps { formatNodeService('backoffice') } }
                stage('Security & Quality') { steps { scanNodeService('backoffice') } }
            }
        }

        stage('CI: Storefront') {
            when { expression { return changedFolders.contains('storefront') } }
            stages {
                stage('Build') { steps { buildNodeService('storefront') } }
                stage('Format Check') { steps { formatNodeService('storefront') } }
                stage('Security & Quality') { steps { scanNodeService('storefront') } }
            }
        }

        // ==========================================
        // CÁC SERVICES MAVEN BFF (Verify)
        // ==========================================
        stage('CI: Backoffice-bff') {
            when { expression { return changedFolders.contains('backoffice-bff') } }
            stages {
                stage('Verify & Checkstyle') { steps { verifyMavenBff('backoffice-bff') } }
                stage('Security & Quality') { steps { scanMavenService('backoffice-bff') } }
            }
        }

        stage('CI: Storefront-bff') {
            when { expression { return changedFolders.contains('storefront-bff') } }
            stages {
                stage('Verify & Checkstyle') { steps { verifyMavenBff('storefront-bff') } }
                stage('Security & Quality') { steps { scanMavenService('storefront-bff') } }
            }
        }

        // ==========================================
        // CÁC SERVICES MAVEN CORE (Install & Test)
        // ==========================================
        stage('CI: Cart') {
            when { expression { return changedFolders.contains('cart') } }
            stages {
                stage('Build') { steps { buildMavenCore('cart') } }
                stage('Test') { steps { testMavenCore('cart') } }
                stage('Security') { steps { scanMavenService('cart') } }
            }
        }

        stage('CI: Customer') {
            when { expression { return changedFolders.contains('customer') } }
            stages {
                stage('Build') { steps { buildMavenCore('customer') } }
                stage('Test') { steps { testMavenCore('customer') } }
                stage('Security') { steps { scanMavenService('customer') } }
            }
        }

        stage('CI: Inventory') {
            when { expression { return changedFolders.contains('inventory') } }
            stages {
                stage('Build') { steps { buildMavenCore('inventory') } }
                stage('Test') { steps { testMavenCore('inventory') } }
                stage('Security') { steps { scanMavenService('inventory') } }
            }
        }

        stage('CI: Location') {
            when { expression { return changedFolders.contains('location') } }
            stages {
                stage('Build') { steps { buildMavenCore('location') } }
                stage('Test') { steps { testMavenCore('location') } }
                stage('Security') { steps { scanMavenService('location') } }
            }
        }

        stage('CI: Media') {
            when { expression { return changedFolders.contains('media') } }
            stages {
                stage('Build') { steps { buildMavenCore('media') } }
                stage('Test') { steps { testMavenCore('media') } }
                stage('Security') { steps { scanMavenService('media') } }
            }
        }

        stage('CI: Order') {
            when { expression { return changedFolders.contains('order') } }
            stages {
                stage('Build') { steps { buildMavenCore('order') } }
                stage('Test') { steps { testMavenCore('order') } }
                stage('Security') { steps { scanMavenService('order') } }
            }
        }

        stage('CI: Payment') {
            when { expression { return changedFolders.contains('payment') } }
            stages {
                stage('Build') { steps { buildMavenCore('payment') } }
                stage('Test') { steps { testMavenCore('payment') } }
                stage('Security') { steps { scanMavenService('payment') } }
            }
        }

        stage('CI: Payment Paypal') {
            when { expression { return changedFolders.contains('payment-paypal') } }
            stages {
                stage('Build') { steps { buildMavenCore('payment-paypal') } }
                stage('Test') { steps { testMavenCore('payment-paypal') } }
                stage('Security') { steps { scanMavenService('payment-paypal') } }
            }
        }

        stage('CI: Product') {
            when { expression { return changedFolders.contains('product') } }
            stages {
                stage('Build') { steps { buildMavenCore('product') } }
                stage('Test') { steps { testMavenCore('product') } }
                stage('Security') { steps { scanMavenService('product') } }
            }
        }

        stage('CI: Promotion') {
            when { expression { return changedFolders.contains('promotion') } }
            stages {
                stage('Build') { steps { buildMavenCore('promotion') } }
                stage('Test') { steps { testMavenCore('promotion') } }
                stage('Security') { steps { scanMavenService('promotion') } }
            }
        }

        stage('CI: Rating') {
            when { expression { return changedFolders.contains('rating') } }
            stages {
                stage('Build') { steps { buildMavenCore('rating') } }
                stage('Test') { steps { testMavenCore('rating') } }
                stage('Security') { steps { scanMavenService('rating') } }
            }
        }

        stage('CI: Recommendation') {
            when { expression { return changedFolders.contains('recommendation') } }
            stages {
                stage('Build') { steps { buildMavenCore('recommendation') } }
                stage('Test') { steps { testMavenCore('recommendation') } }
                stage('Security') { steps { scanMavenService('recommendation') } }
            }
        }

        stage('CI: Sample data') {
            when { expression { return changedFolders.contains('sampledata') } }
            stages {
                stage('Build') { steps { buildMavenCore('sampledata') } }
                stage('Test') { steps { testMavenCore('sampledata') } }
                stage('Security') { steps { scanMavenService('sampledata') } }
            }
        }

        stage('CI: Search') {
            when { expression { return changedFolders.contains('search') } }
            stages {
                stage('Build') { steps { buildMavenCore('search') } }
                stage('Test') { steps { testMavenCore('search') } }
                stage('Security') { steps { scanMavenService('search') } }
            }
        }

        stage('CI: Tax') {
            when { expression { return changedFolders.contains('tax') } }
            stages {
                stage('Build') { steps { buildMavenCore('tax') } }
                stage('Test') { steps { testMavenCore('tax') } }
                stage('Security') { steps { scanMavenService('tax') } }
            }
        }

        stage('CI: Webhook') {
            when { expression { return changedFolders.contains('webhook') } }
            stages {
                stage('Build') { steps { buildMavenCore('webhook') } }
                stage('Test') { steps { testMavenCore('webhook') } }
                stage('Security') { steps { scanMavenService('webhook') } }
            }
        }

        stage('CI: Common-library') {
            when { expression { return changedFolders.contains('common-library') } }
            stages {
                stage('Build') { steps { buildMavenCore('common-library') } }
                stage('Test') { steps { testMavenCore('common-library') } }
                stage('Security') { steps { scanMavenService('common-library') } }
            }
        }

        // ==========================================
        // SERVICE ĐẶC BIỆT: DELIVERY (Kiểm tra điều kiện Test)
        // ==========================================
        stage('CI: Delivery') {
            when { expression { return changedFolders.contains('delivery') } }
            stages {
                stage('Build') { steps { buildMavenCore('delivery') } }
                stage('Test') { steps { testMavenDelivery('delivery') } }
                stage('Security') { steps { scanMavenService('delivery') } }
            }
        }
    }

    // ==========================================
    // DỌN DẸP & THÔNG BÁO SAU KHI CHẠY
    // ==========================================
    post {
        always { cleanWs() }
        success { echo "✅ Pipeline hoàn thành thành công!" }
        failure { echo "❌ Pipeline thất bại!" }
    }
}

// =========================================================================================
// ============================= CÁC HÀM HỖ TRỢ (HELPER METHODS) =============================
// Việc định nghĩa hàm ở đây giúp Jenkins không bị quá tải bộ nhớ khi parse file Declarative
// =========================================================================================

// --- Helpers cho Node.js ---
def buildNodeService(String svc) {
    dir(svc) {
        echo "[INFO] Đang cài đặt thư viện và build ${svc}..."
        sh 'npm ci'
        sh 'npm run build'
    }
}

def formatNodeService(String svc) {
    dir(svc) {
        echo "[INFO] Đang kiểm tra Format..."
        sh 'npm run lint'
        sh 'npx prettier --check .'
    }
}

def scanNodeService(String svc) {
    dir(svc) {
        echo "[INFO] Quét SonarQube..."
        withSonarQubeEnv('Sonar-Server') {
            sh "sonar-scanner -Dsonar.projectKey=${svc} -Dsonar.sources=."
        }
        echo "[INFO] Quét Snyk..."
        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
            sh "snyk test --all-projects --json-file-output=snyk-${svc}-report.json || true"
        }
        archiveArtifacts artifacts: "snyk-${svc}-report.json", allowEmptyArchive: true
    }
}

// --- Helpers cho Maven BFF ---
def verifyMavenBff(String svc) {
    echo "[INFO] Đang chạy Verify và Checkstyle cho ${svc}..."
    sh "mvn clean verify checkstyle:checkstyle -DskipTests -pl ${svc} -am"
}

// --- Helpers cho Maven Core ---
def buildMavenCore(String svc) {
    sh "mvn clean install -DskipTests -pl ${svc} -am"
}

def testMavenCore(String svc) {
    try {
        sh "mvn test jacoco:report -pl ${svc} -am"
    } finally {
        // Khối finally đảm bảo Report luôn được thu thập kể cả khi test fail
        junit testResults: "${svc}/target/surefire-reports/*.xml", allowEmptyResults: true
        if (fileExists("${svc}/target/site/jacoco/jacoco.xml")) {
            recordCoverage(
                tools: [[parser: 'JACOCO', pattern: "${svc}/target/site/jacoco/jacoco.xml"]],
                qualityGates: [
                    [threshold: 70.0, metric: 'LINE', unstable: false],
                    [threshold: 70.0, metric: 'BRANCH', unstable: false]
                ]
            )
        }
    }
}

def scanMavenService(String svc) {
    echo "[INFO] Quét SonarQube cho ${svc}..."
    withSonarQubeEnv('Sonar-Server') {
        sh "mvn sonar:sonar -pl ${svc} -am -Dsonar.projectKey=${svc}"
    }
    echo "[INFO] Quét Snyk cho ${svc}..."
    withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
        sh "snyk test --all-projects --json-file-output=snyk-${svc}-report.json --target-dir=${svc} || true"
    }
    archiveArtifacts artifacts: "snyk-${svc}-report.json", allowEmptyArchive: true
}

// --- Helper đặc thù cho Delivery ---
def testMavenDelivery(String svc) {
    if (fileExists("${svc}/src/test")) {
        echo "[INFO] Phát hiện thư mục test trong ${svc}, đang chạy Test..."
        try {
            sh "mvn test jacoco:report -pl ${svc} -am"
        } finally {
            if (fileExists("${svc}/target/site/jacoco/jacoco.xml")) {
                junit testResults: "${svc}/target/surefire-reports/*.xml", allowEmptyResults: true
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: "${svc}/target/site/jacoco/jacoco.xml"]],
                    qualityGates: [
                        [threshold: 70.0, metric: 'LINE', unstable: false],
                        [threshold: 70.0, metric: 'BRANCH', unstable: false]
                    ]
                )
            }
        }
    } else {
        echo "[INFO] Không tìm thấy thư mục test trong ${svc}, bỏ qua."
    }
}