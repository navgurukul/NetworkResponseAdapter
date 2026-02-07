# Publishing Guide

## How to Publish to JitPack

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Release version 1.0.0"
   git push origin main
   ```

2. **Create a GitHub Release**
   - Go to your GitHub repository
   - Click on "Releases" → "Create a new release"
   - Tag version: `1.0.0`
   - Release title: `v1.0.0`
   - Click "Publish release"

3. **Build on JitPack**
   - Go to https://jitpack.io
   - Enter your repository URL: `https://github.com/navgurukul/NetworkResponseAdapter`
   - Click "Look up"
   - Click "Get it" next to version 1.0.0
   - JitPack will build your library

4. **Use in Projects**
   
   Add to your root build.gradle:
   ```gradle
   allprojects {
       repositories {
           maven { url 'https://jitpack.io' }
       }
   }
   ```
   
   Add dependency:
   ```gradle
   dependencies {
       implementation 'com.github.navgurukul:NetworkResponseAdapter:1.0.0'
   }
   ```

## Local Testing

Test the library locally before publishing:

```bash
./gradlew :networkresponse:publishToMavenLocal
```

Then in another project, add to build.gradle:
```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.github.navgurukul:network-response-adapter:1.0.0'
}
```

## Version Updates

To release a new version:
1. Update version in `networkresponse/build.gradle.kts`
2. Create a new GitHub release with the new version tag
3. JitPack will automatically build the new version
