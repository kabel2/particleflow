#include <jni.h>
#include <cmath>
#include <cstdlib>

static void hsv2rgba(float h, float s, float v, float* rgba) {
    float h6 = 6 * h;
    float r, g, b;
    float coef;

    if (h6 < 1) {
        r = 0; g = 1 - h6; b = 1;
    } else if (h6 < 2) {
        r = h6 - 1; g = 0; b = 1;
    } else if (h6 < 3) {
        r = 1; g = 0; b = 3 - h6;
    } else if (h6 < 4) {
        r = 1; g = h6 - 3; b = 0;
    } else if (h6 < 5) {
        r = 5 - h6; g = 1; b = 0;
    } else {
        r = 0; g = 1; b = h6 - 5;
    }

    coef = v * s;
    rgba[0] = v - coef * r;
    rgba[1] = v - coef * g;
    rgba[2] = v - coef * b;
    rgba[3] = 1.0f;
}

static float getHue(float coef, float sh, float fh, int hueDirection) {
    float hue;
    if (sh < fh && hueDirection == 0) {
        sh += 1;
    } else if (sh > fh && hueDirection == 1) {
        fh += 1;
    }
    hue = (1 - coef) * sh + coef * fh;
    if (hue >= 1) {
        hue -= 1;
    }
    return hue;
}

static float getSaturation(float coef, float ss, float fs) {
    return (1 - coef) * ss + coef * fs;
}

static float getValue(float coef, float sv, float fv) {
    return (1 - coef) * sv + coef * fv;
}

static float getSpeedCoef(float vx, float vy) {
    float coef = logf(vx * vx + vy * vy + 1) / 4.5f;
    if(coef > 1.0f) {
        coef = 1.0f;
    }
    return coef;
}

extern "C" JNIEXPORT void JNICALL
Java_com_nfaralli_particleflow_ParticlesRenderer_nativeInitParticles(
        JNIEnv* env, jobject /*thiz*/,
        jfloatArray posArr, jfloatArray deltaArr, jfloatArray colorArr,
        jint count, jfloat width, jfloat height,
        jfloat slowHue, jfloat slowSaturation, jfloat slowValue,
        jfloat fastHue, jfloat fastSaturation, jfloat fastValue,
        jint hueDirection) {

    jfloat* pos = env->GetFloatArrayElements(posArr, nullptr);
    jfloat* delta = env->GetFloatArrayElements(deltaArr, nullptr);
    jfloat* color = env->GetFloatArrayElements(colorArr, nullptr);

    float radius = sqrtf(width*width + height*height) / 2;
    float r, theta;
    float speedCoef;
    float rgba[4];

    for (int i = 0; i < count; i++) {
        r = radius * sqrtf((float)rand() / RAND_MAX);
        theta = ((float)rand() / RAND_MAX) * 6.28318530718f;
        pos[i*2] = (width/2) + r*cosf(theta);
        pos[i*2+1] = (height/2) + r*sinf(theta);
        delta[i*2] = 0;
        delta[i*2+1] = 0;
        speedCoef = getSpeedCoef(delta[i*2], delta[i*2+1]);
        hsv2rgba(getHue(speedCoef, slowHue, fastHue, hueDirection),
                 getSaturation(speedCoef, slowSaturation, fastSaturation),
                 getValue(speedCoef, slowValue, fastValue),
                 rgba);
        color[i*4] = rgba[0];
        color[i*4+1] = rgba[1];
        color[i*4+2] = rgba[2];
        color[i*4+3] = rgba[3];
    }

    env->ReleaseFloatArrayElements(posArr, pos, 0);
    env->ReleaseFloatArrayElements(deltaArr, delta, 0);
    env->ReleaseFloatArrayElements(colorArr, color, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_nfaralli_particleflow_ParticlesRenderer_nativeUpdateParticles(
        JNIEnv* env, jobject /*thiz*/,
        jfloatArray posArr, jfloatArray deltaArr, jfloatArray colorArr,
        jint count, jfloat width, jfloat height,
        jfloatArray touchArr, jint numTouch,
        jfloat attractionCoef, jfloat dragCoef,
        jfloat slowHue, jfloat slowSaturation, jfloat slowValue,
        jfloat fastHue, jfloat fastSaturation, jfloat fastValue,
        jint hueDirection) {

    jfloat* pos = env->GetFloatArrayElements(posArr, nullptr);
    jfloat* delta = env->GetFloatArrayElements(deltaArr, nullptr);
    jfloat* color = env->GetFloatArrayElements(colorArr, nullptr);
    jfloat* touch = env->GetFloatArrayElements(touchArr, nullptr);

    for (int index = 0; index < count; index++) {
        float speedCoef, theta;
        float diffSqNorm;
        float diffX, diffY;
        float accX = 0, accY = 0;
        float ptX = pos[index*2];
        float ptY = pos[index*2+1];

        for(int i=0; i<numTouch; i++){
            if (touch[i*2] >= 0) {
                diffX = touch[i*2] - ptX;
                diffY = touch[i*2+1] - ptY;
                diffSqNorm = diffX * diffX + diffY * diffY;
                if (diffSqNorm < 0.1f) {
                    theta = ((float)rand() / RAND_MAX) * 6.28318530718f;
                    diffX = cosf(theta);
                    diffY = sinf(theta);
                    diffSqNorm = 1;
                }
                accX += (attractionCoef / diffSqNorm) * diffX;
                accY += (attractionCoef / diffSqNorm) * diffY;
            }
        }

        delta[index*2] += accX;
        delta[index*2+1] += accY;
        ptX += delta[index*2];
        ptY += delta[index*2+1];
        pos[index*2] = ptX;
        pos[index*2+1] = ptY;

        speedCoef = getSpeedCoef(delta[index*2], delta[index*2+1]);
        float rgba[4];
        hsv2rgba(getHue(speedCoef, slowHue, fastHue, hueDirection),
                 getSaturation(speedCoef, slowSaturation, fastSaturation),
                 getValue(speedCoef, slowValue, fastValue),
                 rgba);
        color[index*4] = rgba[0];
        color[index*4+1] = rgba[1];
        color[index*4+2] = rgba[2];
        color[index*4+3] = rgba[3];

        delta[index*2] *= dragCoef;
        delta[index*2+1] *= dragCoef;
    }

    env->ReleaseFloatArrayElements(posArr, pos, 0);
    env->ReleaseFloatArrayElements(deltaArr, delta, 0);
    env->ReleaseFloatArrayElements(colorArr, color, 0);
    env->ReleaseFloatArrayElements(touchArr, touch, JNI_ABORT);
}
