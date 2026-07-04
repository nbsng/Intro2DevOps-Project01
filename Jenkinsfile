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
        // CD: STAGING — BUILD TẤT CẢ SERVICES KHI CÓ GIT TAG v1.2.3
        // Vì khi push Git Tag, changedFolders = [] → không có CI stage nào chạy
        // Cần stage riêng để build Docker image cho TẤT CẢ services với tag release
        // ==========================================
        stage('CD: Release Staging — Build All Services') {
            when {
                expression {
                    return env.TAG_NAME != null && env.TAG_NAME.matches(/v\.?\d+\.\d+\.\d+/)
                }
            }
            steps {
                script {
                    echo "[INFO] 🚀 Git Tag Release ${env.TAG_NAME} — Building ALL services for staging..."
                    def allDockerServices = [
                        'backoffice', 'storefront',
                        'backoffice-bff', 'storefront-bff',
                        'cart', 'customer', 'inventory',
                        'media', 'order',
                        'product', 'sampledata', 'search', 'tax'
                    ]
                    withCredentials([usernamePassword(credentialsId: 'jenkins-dockerhub', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        for (int i = 0; i < allDockerServices.size(); i++) {
                            def svc = allDockerServices[i]
                            def imageName = "sybew/${svc}:${env.TAG_NAME}"
                            if (fileExists("${svc}/Dockerfile")) {
                                echo "[INFO] Building ${imageName}..."
                                sh "docker build -t ${imageName} ./${svc}"
                                sh "docker push ${imageName}"
                                sh "docker rmi ${imageName} || true"
                            } else {
                                echo "[WARN] Không tìm thấy Dockerfile cho ${svc}, bỏ qua."
                            }
                        }
                    }
                    // Sau khi build xong toàn bộ, update deployment repo staging
                    syncAllManifests(allDockerServices, 'staging', env.TAG_NAME)
                }
            }
        }

        // ==========================================
        // CD: DEV — Sync manifest cho các services thay đổi trên main
        // Chỉ update services đã được build trong lần chạy này
        // ==========================================
        stage('CD: Sync Dev Manifests') {
            when {
                expression {
                    // Chạy trên mọi nhánh (trừ khi có Git Tag → đã có stage Staging riêng)
                    // Feature branch: chỉ update service nào có thay đổi với tag = commitId
                    // main branch: update service có thay đổi với tag = "main"
                    return (env.TAG_NAME == null || !env.TAG_NAME.matches(/v\.?\d+\.\d+\.\d+/))
                }
            }
            steps {
                script {
                    def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    def imageTag    = (env.BRANCH_NAME == 'main') ? 'main' : shortCommit
                    def svcToChartMap = [
                        'backoffice': 'backoffice-ui', 'storefront': 'storefront-ui',
                        'backoffice-bff': 'backoffice-bff', 'storefront-bff': 'storefront-bff',
                        'cart': 'cart', 'customer': 'customer', 'inventory': 'inventory',
                        'media': 'media', 'order': 'order',
                        'product': 'product', 'sampledata': 'sampledata',
                        'search': 'search', 'tax': 'tax'
                    ]
                    def changedCharts = []
                    changedFolders.each { svc ->
                        if (svcToChartMap.containsKey(svc)) {
                            changedCharts.add(svcToChartMap[svc])
                        }
                    }
                    // Nếu có thay đổi trong k8s/ → sync toàn bộ (cập nhật cấu hình Helm chart)
                    // [FIX] Bỏ changedFolders.isEmpty(): trước đây khi push commit rỗng sẽ
                    // sync toàn bộ với tag shortCommit chưa tồn tại → CrashLoopBackOff
                    if (changedFolders.contains('k8s')) {
                        changedCharts = CHART_VALUE_KEYS.keySet().toList()
                    }
                    if (!changedCharts.isEmpty()) {
                        echo "[INFO] 🚀 Syncing ${changedCharts} → dev with tag: ${imageTag}"
                        syncAllManifests(changedCharts, 'dev', imageTag)
                    } else {
                        echo "[INFO] Không có service Docker nào thay đổi, bỏ qua sync manifest."
                    }
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

// --- Helper Docker Build & Push (chỉ build + push image, KHÔNG update manifest) ---
// Manifest được update tập trung bởi syncAllManifests() ở stage CD
def buildAndPushDocker(String svc) {
    script {
        def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        // Khi gọi từ CI stage (changedFolders), luôn là luồng dev
        // Luồng staging (TAG_NAME) sẽ không đi qua đây vì CI stages bị skip khi tag push
        def imageTag  = (env.BRANCH_NAME == 'main') ? 'main' : shortCommit
        def imageName = "sybew/${svc}:${imageTag}"

        echo "[INFO] Building Docker image: ${imageName}"
        sh "docker build -t ${imageName} ./${svc}"

        echo "[INFO] Pushing to Docker Hub: ${imageName}"
        withCredentials([usernamePassword(credentialsId: 'jenkins-dockerhub', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
            sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
            sh "docker push ${imageName}"
            sh "docker rmi ${imageName} || true"
        }
        // Manifest update được xử lý tập trung tại stage 'CD: Sync Dev Manifests'
    }
}

// =========================================================================================
// === HELPER: JENKINS CHỈ UPDATE VALUES.YAML TRONG DEPLOYMENT REPO =======================
// =========================================================================================
// Kiến trúc GitOps đúng (ArgoCD multi-source, v2.6+):
//
//   Intro2DevOps-Project01 (Source Repo)       Intro2DevOps-Project02Deployment (Deploy Repo)
//   ├── k8s/charts/cart/                        ├── dev/
//   │   ├── Chart.yaml  ← Helm templates        │   ├── cart/
//   │   ├── values.yaml ← default values        │   │   └── values.yaml ← CHỈ override tag
//   │   └── templates/                          │   ├── backoffice-ui/
//   └── ...                                     │   │   └── values.yaml
//                                               │   └── ...
//                                               └── staging/
//                                                   └── ...
//
//   Jenkins chỉ làm một việc: update tag trong deployment repo sau khi push Docker image.
//   ArgoCD dùng "sources" (multi-source) để kết hợp:
//     source[0] → chart từ Source Repo (k8s/charts/<svc>/)
//     source[1] → values từ Deploy Repo   (dev/<svc>/values.yaml)
//
// NOTE: Các ArgoCD Application CRDs được tạo 1 lần bằng hàm initArgoCDApplications()
//       Sau đó Jenkins CHỈ update values.yaml — ArgoCD tự phát hiện và sync.
// =========================================================================================

// Map: chart name → top-level key trong values.yaml (để ghi đúng path)
// ui charts dùng "ui.image.tag", backend charts dùng "backend.image.tag"
@groovy.transform.Field
def CHART_VALUE_KEYS = [
    'backoffice-ui'   : 'ui',
    'storefront-ui'   : 'ui',
    'backoffice-bff'  : 'backend',
    'storefront-bff'  : 'backend',
    'cart'            : 'backend',
    'customer'        : 'backend',
    'inventory'       : 'backend',
    'media'           : 'backend',
    'order'           : 'backend',
    'product'         : 'backend',
    'sampledata'      : 'backend',
    'search'          : 'backend',
    'tax'             : 'backend'
]

// --- Được gọi bởi các stage CD sau khi Docker image đã được push ---
// Params:
//   charts    : List<String> tên chart cần update (từ CHART_VALUE_KEYS)
//   targetEnv : "dev" hoặc "staging"
//   imageTag  : tag image đã push lên Docker Hub
def syncAllManifests(List<String> charts, String targetEnv, String imageTag) {
    script {
        def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

        withCredentials([usernamePassword(credentialsId: 'jenkins-github-manifest-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
            sh 'rm -rf Intro2DevOps-Project02Deployment'
            sh 'git clone https://${GIT_USER}:${GIT_TOKEN}@github.com/nbsng/Intro2DevOps-Project02Deployment.git Intro2DevOps-Project02Deployment'

            // Chỉ update các chart được truyền vào (không overwrite service khác)
            for (int i = 0; i < charts.size(); i++) {
                def chart    = charts[i]
                def valueKey = CHART_VALUE_KEYS[chart]
                if (!valueKey) {
                    echo "[WARN] Chart '${chart}' không có trong CHART_VALUE_KEYS, bỏ qua."
                    continue
                }
                def valuesDir  = "Intro2DevOps-Project02Deployment/${targetEnv}/${chart}"
                def valuesFile = "${valuesDir}/values.yaml"
                sh "mkdir -p ${valuesDir}"

                def content = """\
# Auto-generated by Jenkins CI — DO NOT EDIT MANUALLY
# Branch: ${env.BRANCH_NAME ?: 'N/A'} | Commit: ${shortCommit}
${valueKey}:
  image:
    tag: "${imageTag}"
"""
                writeFile file: valuesFile, text: content
                echo "[INFO] ✅ ${targetEnv}/${chart} → tag: ${imageTag}"
            }

            def commitMsg = "ci(${targetEnv}): update [${charts.join(', ')}] to tag ${imageTag} [skip ci]"
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
            echo "[INFO] ✅ Deployment repo updated → ArgoCD sẽ tự phát hiện và sync."
        }
    }
}

// --- Hàm khởi tạo 1 lần: tạo ArgoCD Application CRDs dùng multi-source ---
// Chạy thủ công 1 lần lúc setup, KHÔNG chạy mỗi lần build
// Yêu cầu: argocd CLI đã login vào cluster
def initArgoCDApplications(String targetEnv = 'dev') {
    script {
        CHART_VALUE_KEYS.each { chart, valueKey ->
            def appName = "${chart}-${targetEnv}"
            sh """
                argocd app create ${appName} \\
                  --project default \\
                  --dest-server https://kubernetes.default.svc \\
                  --dest-namespace ${targetEnv} \\
                  --sync-policy automated \\
                  --auto-prune \\
                  --self-heal \\
                  --sync-option CreateNamespace=true \\
                  --source-0-repo    https://github.com/nbsng/Intro2DevOps-Project01.git \\
                  --source-0-revision main \\
                  --source-0-path    k8s/charts/${chart} \\
                  --source-0-helm-value-files values.yaml \\
                  --source-1-repo    https://github.com/nbsng/Intro2DevOps-Project02Deployment.git \\
                  --source-1-revision main \\
                  --source-1-ref     valuesRepo \\
                  --upsert || true
                echo "[INFO] ArgoCD App created/updated: ${appName}"
            """
        }
    }
}
