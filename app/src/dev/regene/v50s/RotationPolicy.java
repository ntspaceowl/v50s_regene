package dev.regene.v50s;

/** Gravity-based pose with hysteresis; a flat phone retains its last pose. */
final class RotationPolicy {
    private boolean landscape;
    RotationPolicy(boolean initialLandscape) { landscape=initialLandscape; }
    boolean sample(float x,float y) {
        float ax=Math.abs(x), ay=Math.abs(y);
        if(ax>6 && ay<4) landscape=true;
        else if(ay>6 && ax<4) landscape=false;
        return landscape;
    }
    boolean landscape() {return landscape;}
}
