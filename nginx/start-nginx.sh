#!/bin/sh
set -eu

watch_certificate() {
    applied_checksum="$1"
    while sleep 60; do
        if ! current_checksum=$(sha256sum "$certificate_path"); then
            echo "[certificate-watch] Cannot read certificate; retrying in 60s." >&2
            continue
        fi
        [ "$current_checksum" != "$applied_checksum" ] || continue

        if nginx -t && nginx -s reload; then
            applied_checksum="$current_checksum"
            echo "[certificate-watch] Certificate changed; nginx reload requested."
        else
            echo "[certificate-watch] Reload failed; retrying in 60s." >&2
        fi
    done
}

if [ "${1:-}" = "nginx" ]; then
    certificate_path=/etc/letsencrypt/live/urlcut.kr/fullchain.pem
    # Capture before starting nginx so a concurrent renewal is detected too.
    initial_checksum=$(sha256sum "$certificate_path")
    watch_certificate "$initial_checksum" &
    echo "[certificate-watch] Checking for certificate changes every 60s."
fi

# Preserve the image's initialization and keep nginx as PID 1 for shutdown signals.
exec /docker-entrypoint.sh "$@"
