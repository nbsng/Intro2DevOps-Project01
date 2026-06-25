#!/bin/bash
set -x

# Auto restart when change configmap or secret
helm repo add stakater https://stakater.github.io/stakater-charts
helm repo update

read -rd '' DOMAIN \
< <(yq -r '.domain' ./cluster-config.yaml)

GLOBAL_ARGS="--set global.domain=$DOMAIN"

helm dependency build ../charts/backoffice-bff
helm upgrade --install backoffice-bff ../charts/backoffice-bff \
--namespace yas --create-namespace \
--set ingress.host="backoffice.$DOMAIN" \
--set host="backoffice.$DOMAIN" \
$GLOBAL_ARGS

helm dependency build ../charts/backoffice-ui
helm upgrade --install backoffice-ui ../charts/backoffice-ui \
--namespace yas --create-namespace \
$GLOBAL_ARGS

sleep 60

helm dependency build ../charts/storefront-bff
helm upgrade --install storefront-bff ../charts/storefront-bff \
--namespace yas --create-namespace \
--set ingress.host="storefront.$DOMAIN" \
--set host="storefront.$DOMAIN" \
$GLOBAL_ARGS

helm dependency build ../charts/storefront-ui
helm upgrade --install storefront-ui ../charts/storefront-ui \
--namespace yas --create-namespace \
--set API_BASE_PATH="http://storefront.$DOMAIN/api" \
$GLOBAL_ARGS

sleep 60

helm upgrade --install swagger-ui ../charts/swagger-ui \
--namespace yas --create-namespace \
--set ingress.host="api.$DOMAIN" \
$GLOBAL_ARGS

sleep 20

for chart in {"cart","customer","inventory","media","order","product","rating","search","tax","recommendation","sampledata"} ; do
    helm dependency build ../charts/"$chart"
    
    # Đối với riêng dịch vụ media, cần đè thêm publicUrl nếu chart con có hỗ trợ
    EXTRA_ARGS=""
    if [ "$chart" == "media" ]; then
        EXTRA_ARGS="--set ingress.host=api.$DOMAIN"
    fi

    helm upgrade --install "$chart" ../charts/"$chart" \
    --namespace yas --create-namespace \
    --set backend.ingress.host="api.$DOMAIN" \
    $EXTRA_ARGS \
    $GLOBAL_ARGS
    sleep 60
done