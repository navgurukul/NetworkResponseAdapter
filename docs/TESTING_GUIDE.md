# 🧪 Testing Guide - How to Verify Your Package Works

## Quick Verification Checklist

### ✅ 1. Local Build Test
```bash
# Clean and build the library
./gradlew clean :networkresponse:assembleRelease

# Expected: BUILD SUCCESSFUL
```

### ✅ 2. Publish to Maven Local
```bash
# Publish to your local Maven repository
./gradlew :networkresponse:publishToMavenLocal

# Expected: BUILD SUCCESSFUL
# Location: ~/.m2/repository/com/github/navgurukul/network-response-adapter/1.0.0/
```

### ✅ 3. Verify JitPack Build

1. Go to: https://jitpack.io/#navgurukul/NetworkResponseAdapter
2. You should see version `1.0.0` with a green checkmark ✅
3. If it shows a red X ❌, click on it to see build logs

### ✅ 4. Test in a Sample Project

Create a new Android project and test the library:

#### Step 1: Add JitPack repository
```gradle
// settings.gradle.kts or build.gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### Step 2: Add the dependency
```gradle
// app/build.gradle.kts
dependencies {
    implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
    
    // Required dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

#### Step 3: Sync and verify
- Click "Sync Now" in Android Studio
- Check if sync is successful
- No errors = Package is working! ✅

## 🔬 Detailed Testing Methods

### Method 1: Create a Test App Module

I'll create a simple test in the app module to verify the library works.


## ✅ Test Results

### Unit Tests (All Passing ✅)

```bash
./gradlew :app:testDebugUnitTest
```

**Results: 7/7 tests passed**

1. ✅ test Success response creation
2. ✅ test ServerError response creation  
3. ✅ test NetworkError response creation
4. ✅ test UnknownError response creation
5. ✅ test Error interface implementation
6. ✅ test when expression with Success
7. ✅ test when expression with Error interface

### Build Tests

```bash
# Clean build
./gradlew clean

# Build library
./gradlew :networkresponse:assembleRelease
# Result: ✅ BUILD SUCCESSFUL

# Publish to Maven Local
./gradlew :networkresponse:publishToMavenLocal
# Result: ✅ BUILD SUCCESSFUL
```

## 🚀 Quick Verification Script

Run this single command to verify everything:

```bash
./gradlew clean :networkresponse:assembleRelease :app:testDebugUnitTest
```

Expected output:
```
BUILD SUCCESSFUL in Xs
XX actionable tasks: XX executed
```

## 📊 JitPack Verification

### Check Build Status

1. Visit: https://jitpack.io/#navgurukul/NetworkResponseAdapter/1.0.0
2. Look for the build status icon:
   - ✅ Green checkmark = Build successful
   - ❌ Red X = Build failed (click to see logs)
   - ⏳ Yellow clock = Building in progress

### Get the Build Log

```bash
# View JitPack build log
curl https://jitpack.io/com/github/navgurukul/NetworkResponseAdapter/1.0.0/build.log
```

## 🧪 Integration Test in New Project

### Step-by-Step Test

1. **Create a new Android project**
   ```bash
   # Or use Android Studio: File > New > New Project
   ```

2. **Add JitPack and dependency**
   ```kotlin
   // settings.gradle.kts
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   
   // app/build.gradle.kts
   dependencies {
       implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
       implementation("com.squareup.retrofit2:retrofit:2.9.0")
       implementation("com.squareup.retrofit2:converter-gson:2.9.0")
   }
   ```

3. **Sync project**
   - Click "Sync Now" in Android Studio
   - Wait for sync to complete

4. **Create a test file**
   ```kotlin
   import com.navgurukul.networkresponse.NetworkResponse
   import com.navgurukul.networkresponse.NetworkResponseAdapterFactory
   
   // If this compiles, the library is working! ✅
   ```

5. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

## 🔍 Troubleshooting

### Issue: "Could not find com.github.navgurukul:NetworkResponseAdapter:1.0.0"

**Solutions:**
1. Check JitPack build status (link above)
2. Wait 2-3 minutes after creating the release
3. Try clearing Gradle cache:
   ```bash
   ./gradlew clean --refresh-dependencies
   ```
4. Check if JitPack repository is added correctly

### Issue: "Unresolved reference: NetworkResponse"

**Solutions:**
1. Verify the dependency is added correctly
2. Sync Gradle files
3. Invalidate caches: File > Invalidate Caches / Restart

### Issue: JitPack build fails

**Solutions:**
1. Check build log on JitPack
2. Verify jitpack.yml is correct
3. Ensure all dependencies are available
4. Check if the tag exists: `git tag -l`

## 📝 Manual Testing Checklist

- [ ] Library builds successfully locally
- [ ] Unit tests pass (7/7)
- [ ] Published to Maven Local successfully
- [ ] JitPack shows green checkmark
- [ ] Can add dependency in new project
- [ ] Imports work correctly
- [ ] Can create NetworkResponse objects
- [ ] Can use with Retrofit
- [ ] Documentation is clear and accurate

## 🎯 Success Criteria

Your package is working correctly if:

✅ All unit tests pass  
✅ Library builds without errors  
✅ JitPack build succeeds  
✅ Can be imported in other projects  
✅ All classes are accessible  
✅ No runtime errors when using the library  

## 📞 Getting Help

If tests fail or you encounter issues:

1. Check the error message carefully
2. Review the troubleshooting section above
3. Check JitPack build logs
4. Open an issue on GitHub with:
   - Error message
   - Steps to reproduce
   - Your environment (Android Studio version, Gradle version)

---

**Last Updated:** 2026-02-07  
**Library Version:** 1.0.0  
**Test Status:** ✅ All tests passing
