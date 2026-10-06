# ReGene launcher icon

Approved direction: an open dual-screen handheld that also suggests a book. Midnight navy background, mint casing, warm ivory screens, understated controls and no lettering. The two screens remain the primary silhouette at launcher size.

Generated with the built-in image generation tool. Final source asset: app/res/drawable-nodpi/regene_icon_art.png. Prompt: a single square Android launcher icon, a simplified open Nintendo DS inspired clamshell seen slightly from above, exactly two ivory screens separated by a hinge, mint casing, navy D-pad and two buttons, restrained dimensional shading, no text or logos, generous navy margins for circular cropping.

Implementation: use an adaptive Android icon with a navy background and a 12dp inset for the generated artwork. Both normal and round launcher icons reference the same adaptive resource. Minimum Android API is 26, so an additional legacy icon is unnecessary. ReGene 0.1.20/code21 changes only the launcher artwork and version metadata.

Validation: build and signing verification, inspect packaged icon resource and install the updated APK on V50S. Preserve the development key and final release APK; remove generated build intermediates after verification.
