package com.requiredmod.requiredmod.update;

public record UpdateInfo(
        String version,
        String minecraft,
        String loader,
        String downloadUrl,
        String sha256,
        String changelog,
        String releasePage
) {
}
