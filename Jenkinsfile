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

                        // [FIX] Ép Jenkins fetch đích danh nhánh target của PR về local
                        sh "git fetch origin ${env.CHANGE_TARGET}:refs/remotes/origin/${env.CHANGE_TARGET} --no-tags || true"
                    } else if (currentBuild.previousBuild == null) {
                        echo "[INFO] Nhánh mới được tạo (First Build). Đang dò tìm nhánh mẹ gần nhất..."

                        baseRef = sh(script: '''#!/bin/bash
                            # 1. Tắt cơ chế tự sập Pipeline khi có lỗi shell
                            set +e

                            # 2. Tải TẤT CẢ các nhánh từ remote về local để thuật toán có data đối chiếu
                            # Ép tải vào refs/remotes/origin/* và giấu log đi để không làm hỏng kết quả trả về
                            git fetch origin '+refs/heads/*:refs/remotes/origin/*' --no-tags > /dev/null 2>&1

                            # 3. Thuật toán dò tìm nhánh mẹ
                            CURRENT_BRANCH=${BRANCH_NAME}
                            git for-each-ref --format='%(refname:short)' refs/remotes/origin/ | grep -v "origin/${CURRENT_BRANCH}" | while read branch; do
                                mb=$(git merge-base HEAD "$branch" 2>/dev/null)
                                if [ -n "$mb" ]; then
                                    dist=$(git rev-list --count "$mb"..HEAD 2>/dev/null)
                                    echo "$dist $mb $branch"
                                fi
                            done | sort -n | head -n 1 | awk '{print $2}'
                        ''', returnStdout: true).trim()

                        if (!baseRef) {
                            baseRef = "HEAD~1" 
                        }
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
                stage('Docker Build & Push') { steps { buildAndPushDocker('backoffice') } }
            }
        }

        stage('CI: Storefront') {
            when { expression { return changedFolders.contains('storefront') } }
            stages {
                stage('Build') { steps { buildNodeService('storefront') } }
                stage('Format Check') { steps { formatNodeService('storefront') } }
                stage('Security & Quality') { steps { scanNodeService('storefront') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('storefront') } }
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
                stage('Docker Build & Push') { steps { buildAndPushDocker('backoffice-bff') } }
            }
        }

        stage('CI: Storefront-bff') {
            when { expression { return changedFolders.contains('storefront-bff') } }
            stages {
                stage('Verify & Checkstyle') { steps { verifyMavenBff('storefront-bff') } }
                stage('Security & Quality') { steps { scanMavenService('storefront-bff') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('storefront-bff') } }
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
                stage('Docker Build & Push') { steps { buildAndPushDocker('cart') } }
            }
        }

        stage('CI: Customer') {
            when { expression { return changedFolders.contains('customer') } }
            stages {
                stage('Build') { steps { buildMavenCore('customer') } }
                stage('Test') { steps { testMavenCore('customer') } }
                stage('Security') { steps { scanMavenService('customer') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('customer') } }
            }
        }

        stage('CI: Inventory') {
            when { expression { return changedFolders.contains('inventory') } }
            stages {
                stage('Build') { steps { buildMavenCore('inventory') } }
                stage('Test') { steps { testMavenCore('inventory') } }
                stage('Security') { steps { scanMavenService('inventory') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('inventory') } }
            }
        }

        stage('CI: Location') {
            when { expression { return changedFolders.contains('location') } }
            stages {
                stage('Build') { steps { buildMavenCore('location') } }
                stage('Test') { steps { testMavenCore('location') } }
                stage('Security') { steps { scanMavenService('location') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('location') } }
            }
        }

        stage('CI: Media') {
            when { expression { return changedFolders.contains('media') } }
            stages {
                stage('Build') { steps { buildMavenCore('media') } }
                stage('Test') { steps { testMavenCore('media') } }
                stage('Security') { steps { scanMavenService('media') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('media') } }
            }
        }

        stage('CI: Order') {
            when { expression { return changedFolders.contains('order') } }
            stages {
                stage('Build') { steps { buildMavenCore('order') } }
                stage('Test') { steps { testMavenCore('order') } }
                stage('Security') { steps { scanMavenService('order') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('order') } }
            }
        }

        stage('CI: Payment') {
            when { expression { return changedFolders.contains('payment') } }
            stages {
                stage('Build') { steps { buildMavenCore('payment') } }
                stage('Test') { steps { testMavenCore('payment') } }
                stage('Security') { steps { scanMavenService('payment') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('payment') } }
            }
        }

        stage('CI: Payment Paypal') {
            when { expression { return changedFolders.contains('payment-paypal') } }
            stages {
                stage('Build') { steps { buildMavenCore('payment-paypal') } }
                stage('Test') { steps { testMavenCore('payment-paypal') } }
                stage('Security') { steps { scanMavenService('payment-paypal') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('payment-paypal') } }
            }
        }

        stage('CI: Product') {
            when { expression { return changedFolders.contains('product') } }
            stages {
                stage('Build') { steps { buildMavenCore('product') } }
                stage('Test') { steps { testMavenCore('product') } }
                stage('Security') { steps { scanMavenService('product') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('product') } }
            }
        }

        stage('CI: Promotion') {
            when { expression { return changedFolders.contains('promotion') } }
            stages {
                stage('Build') { steps { buildMavenCore('promotion') } }
                stage('Test') { steps { testMavenCore('promotion') } }
                stage('Security') { steps { scanMavenService('promotion') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('promotion') } }
            }
        }

        stage('CI: Rating') {
            when { expression { return changedFolders.contains('rating') } }
            stages {
                stage('Build') { steps { buildMavenCore('rating') } }
                stage('Test') { steps { testMavenCore('rating') } }
                stage('Security') { steps { scanMavenService('rating') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('rating') } }
            }
        }

        stage('CI: Recommendation') {
            when { expression { return changedFolders.contains('recommendation') } }
            stages {
                stage('Build') { steps { buildMavenCore('recommendation') } }
                stage('Test') { steps { testMavenCore('recommendation') } }
                stage('Security') { steps { scanMavenService('recommendation') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('recommendation') } }
            }
        }

        stage('CI: Sample data') {
            when { expression { return changedFolders.contains('sampledata') } }
            stages {
                stage('Build') { steps { buildMavenCore('sampledata') } }
                stage('Test') { steps { testMavenCore('sampledata') } }
                stage('Security') { steps { scanMavenService('sampledata') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('sampledata') } }
            }
        }

        stage('CI: Search') {
            when { expression { return changedFolders.contains('search') } }
            stages {
                stage('Build') { steps { buildMavenCore('search') } }
                stage('Test') { steps { testMavenCore('search') } }
                stage('Security') { steps { scanMavenService('search') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('search') } }
            }
        }

        stage('CI: Tax') {
            when { expression { return changedFolders.contains('tax') } }
            stages {
                stage('Build') { steps { buildMavenCore('tax') } }
                stage('Test') { steps { testMavenCore('tax') } }
                stage('Security') { steps { scanMavenService('tax') } }
                stage('Docker Build & Push') { steps { buildAndPushDocker('tax') } }
            }
        }

        stage('CI: Webhook') {
            when { expression { return changedFolders.contains('webhook') } }
            stages {
                stage('Build') { steps { buildMavenCore('webhook') } }
                stage('Test') { steps { testMavenCore('webhook') } }
                stage('Security') { steps { scanMavenService('webhook') } }
                // [ĐÃ SỬA] Bổ sung Docker Build & Push cho Webhook service đúng kiến trúc deploy
                stage('Docker Build & Push') { steps { buildAndPushDocker('webhook') } }
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

        // ==========================================
        // CD: ĐỒNG BỘ HELM CHARTS SANG REPO MANIFEST (ARGOCD)
        // Chạy độc lập, không phụ thuộc changedFolders của service
        // Kích hoạt khi: có thay đổi trong k8s/ HOẶC đang trên nhánh argocd-setup
        // ==========================================
        stage('CD: Sync ArgoCD Manifests') {
            when {
                expression {
                    return changedFolders.contains('k8s') ||
                           env.BRANCH_NAME == 'main' ||
                           (env.BRANCH_NAME != null && env.BRANCH_NAME.startsWith('feat/argocd')) ||
                           (env.TAG_NAME != null && env.TAG_NAME.matches(/v\d+\.\d+\.\d+/))
                }
            }
            steps {
                script {
                    syncAllManifests()
                }
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

// --- Helper Docker Build & Push ---
def buildAndPushDocker(String svc) {
    script {
        def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

        // 1. Xác định Tag Image và môi trường dựa vào nhánh hoặc Git Tag
        def imageTag = ""
        def targetEnv = ""
        def isRelease = false

        if (env.TAG_NAME && env.TAG_NAME.matches(/v\d+\.\d+\.\d+/)) {
            // Nếu Jenkins được kích hoạt bởi một Git Tag hệ thống (v1.2.3)
            imageTag = env.TAG_NAME
            targetEnv = "staging"
            isRelease = true
        } else {
            // Mặc định cho nhánh dev/main
            imageTag = (env.BRANCH_NAME == 'main') ? "main" : shortCommit
            targetEnv = "dev"
        }

        def dockerUser = 'sybew'
        def imageName = "${dockerUser}/${svc}:${imageTag}"

        // ... [Giữ nguyên phần build và push Docker hiện tại của bạn] ...
        echo "[INFO] Đang push lên Docker Hub..."
        withCredentials([usernamePassword(credentialsId: 'jenkins-dockerhub', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
            sh "docker push ${imageName}"
            sh "docker rmi ${imageName} || true"
        }

        // ==========================================================================
        // SCRIPT ĐỒNG BỘ SANG REPO MANIFEST CỦA NHÓM BẰNG TOKEN CỦA BẠN (HTTPS)
        // ==========================================================================
        echo "[INFO] Tiến hành cập nhật cấu hình YAML sang Repo Manifest cho môi trường: ${targetEnv}"

        // Jenkins tự động bốc Username (xuxinhno1) và Token (ghp_...) của bạn từ Credentials ra
        withCredentials([usernamePassword(credentialsId: 'jenkins-github-manifest-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
            script {
                // Đường dẫn HTTPS đã cấu hình chính xác theo repo của bạn mình (nbsng)
                def manifestRepoHttps = "https://${GIT_USER}:${GIT_TOKEN}@github.com/nbsng/Intro2DevOps-Project02Deployment.git"

                // 1. Dọn dẹp không gian tạm và clone repo manifest về
                sh 'rm -rf Intro2DevOps-Project02Deployment'
                sh "git clone ${manifestRepoHttps} Intro2DevOps-Project02Deployment"

                // 2. Tạo thư mục cấu trúc môi trường và service nếu chưa có
                sh "mkdir -p Intro2DevOps-Project02Deployment/${targetEnv}/${svc}"

                // 3. Copy toàn bộ file Helm Chart gốc từ k8s/chart/<service> sang repo manifest
                sh "cp -r k8s/chart/${svc}/* Intro2DevOps-Project02Deployment/${targetEnv}/${svc}/"

                // 4. Sửa tag image tự động trong file values.yaml của Helm
                def valuesPath = "Intro2DevOps-Project02Deployment/${targetEnv}/${svc}/values.yaml"
                if (sh(script: "[ -f ${valuesPath} ]", returnStatus: true) == 0) {
                    echo "[INFO] Đang cập nhật tag: \"${imageTag}\" vào file values.yaml..."
                    sh "sed -i 's|tag:.*|tag: \"${imageTag}\"|g' ${valuesPath}"
                }

                // 5. Commit và Push ngược trở lại repo nbsng/Intro2DevOps-Project02Deployment
                dir('Intro2DevOps-Project02Deployment') {
                    sh """
                git config user.name 'Jenkins Automated CI'
                git config user.email 'xuxinhno1@users.noreply.github.com'
                git add .
                git commit -m 'ArgoCD Auto-update: ${svc} to version ${imageTag} in ${targetEnv} [skip ci]'

                # Ép remote sử dụng URL chứa token bảo mật để push không cần gõ pass
                git remote set-url origin ${manifestRepoHttps}
                git push origin main
            """

                    // Nếu chạy luồng Staging (khi có Git Tag hệ thống), thực hiện đánh tag đồng bộ
                    if (isRelease) {
                        echo "[INFO] Đánh Git Tag ${imageTag} đồng bộ sang Repo Manifest..."
                        sh """
                    git tag ${imageTag}
                    git push origin ${imageTag}
                """
                    }
                }
            }
        }
    }
}

// --- Helper: Đồng bộ toàn bộ Helm Charts sang Manifest Repo (ArgoCD) ---
def syncAllManifests() {
    script {
        def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

        // Xác định môi trường dựa trên nhánh/tag
        def targetEnv = ""
        def imageTag = ""
        if (env.TAG_NAME && env.TAG_NAME.matches(/v\d+\.\d+\.\d+/)) {
            targetEnv = "staging"
            imageTag = env.TAG_NAME
        } else {
            targetEnv = "dev"
            imageTag = (env.BRANCH_NAME == 'main') ? "main" : shortCommit
        }

        // Danh sách các chart folder thực tế trong k8s/charts/
        def chartFolders = [
            'backoffice-ui',
            'storefront-ui',
            'backoffice-bff',
            'storefront-bff',
            'cart',
            'customer',
            'inventory',
            'location',
            'media',
            'order',
            'payment',
            'payment-paypal',
            'product',
            'promotion',
            'rating',
            'recommendation',
            'sampledata',
            'search',
            'tax',
            'webhook'
        ]

        withCredentials([usernamePassword(credentialsId: 'jenkins-github-manifest-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
            // [FIX] Dùng single-quote sh để tránh Groovy string interpolation với secret GIT_TOKEN
            sh 'rm -rf Intro2DevOps-Project02Deployment'
            sh 'git clone https://${GIT_USER}:${GIT_TOKEN}@github.com/nbsng/Intro2DevOps-Project02Deployment.git Intro2DevOps-Project02Deployment'

            // Duyệt qua từng chart folder và copy sang manifest repo
            for (int i = 0; i < chartFolders.size(); i++) {
                def chart = chartFolders[i]
                // [FIX] Đường dẫn đúng là k8s/charts/ (có chữ s)
                def chartSrc = "k8s/charts/${chart}"
                def chartDest = "Intro2DevOps-Project02Deployment/${targetEnv}/${chart}"

                if (fileExists(chartSrc)) {
                    echo "[INFO] Đang sync Helm chart: ${chart} -> ${targetEnv}/"
                    sh "mkdir -p ${chartDest}"
                    sh "cp -r ${chartSrc}/. ${chartDest}/"

                    // [FIX] Pattern sed khớp với bất kỳ indent level
                    def valuesPath = "${chartDest}/values.yaml"
                    if (fileExists(valuesPath)) {
                        sh "sed -i \"s|^\\(\\s*tag:\\s*\\).*|\\1${imageTag}|\" ${valuesPath}"
                    }
                } else {
                    echo "[WARN] Không tìm thấy ${chartSrc}, bỏ qua."
                }
            }

            // [FIX] Dùng \${GIT_USER} và \${GIT_TOKEN} (escape $) trong double-quote sh
            def commitMsg = "ArgoCD Sync: full manifest update for ${targetEnv} at ${imageTag} [skip ci]"
            dir('Intro2DevOps-Project02Deployment') {
                sh """
                    git config user.name 'Jenkins Automated CI'
                    git config user.email 'xuxinhno1@users.noreply.github.com'
                    git add .
                    git diff --cached --quiet || git commit -m '${commitMsg}'
                    git remote set-url origin https://\${GIT_USER}:\${GIT_TOKEN}@github.com/nbsng/Intro2DevOps-Project02Deployment.git
                    git push origin main
                """
            }
        }
    }
}
