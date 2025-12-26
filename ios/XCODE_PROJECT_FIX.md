# Xcode Project Fix Instructions

**Issue**: Multiple commands produce error due to duplicate file references  
**Status**: Manual fix required before testing

## Problem

The `update_project.rb` script added source files multiple times to the Xcode project, causing build failures:

```
error: Multiple commands produce '/path/to/TunerView.stringsdata'
error: Multiple commands produce '/path/to/TuningBarView.stringsdata'
... (and others)
```

## Quick Fix (Recommended)

### Option 1: Manual Cleanup in Xcode

1. **Open Project**
   ```bash
   cd /Users/carlosalbertoruizrobles/Desarrollos/my-tune/ios
   open MyTune.xcodeproj
   ```

2. **Remove Duplicate References**
   - In Project Navigator (left panel), look for duplicate file entries
   - Files that may have duplicates:
     - `TunerView.swift`
     - `TuningBarView.swift`
     - `CentDisplayView.swift`
     - `StringIndicatorsView.swift`
     - `NoteDisplayView.swift`
     - `ThemeProvider.swift`
     - All Model files (Tuning, GuitarString, PitchResult, TunerSettings)
     - All Service files (FFTProcessor, HPSPitchDetector, etc.)
   
3. **For Each Duplicate**:
   - Right-click on the duplicate entry
   - Select "Remove Reference" (NOT "Move to Trash")
   - Keep only ONE reference per file

4. **Clean Build Folder**
   - Go to: Product → Clean Build Folder
   - Or press: Cmd + Shift + K

5. **Rebuild**
   - Go to: Product → Build
   - Or press: Cmd + B

6. **Verify Success**
   - Build should complete without "Multiple commands produce" errors
   - Check for any remaining compilation errors

### Option 2: Automated Fix Script

If manual cleanup is tedious, use this command to regenerate the project file cleanly:

```bash
cd /Users/carlosalbertoruizrobles/Desarrollos/my-tune/ios

# Backup current project file
cp -r MyTune.xcodeproj MyTune.xcodeproj.backup

# Create clean project script
cat > clean_project.rb << 'EOF'
#!/usr/bin/env ruby
require 'xcodeproj'

project_path = 'MyTune.xcodeproj'
project = Xcodeproj::Project.open(project_path)
target = project.targets.first

# Remove all source file references from build phases
target.source_build_phase.files.clear

# Re-add files (will automatically deduplicate)
main_group = project.main_group['MyTune']

# Helper to add files without duplicates
def add_file_if_exists(group, file_path, target)
  file_ref = group.files.find { |f| f.path == file_path }
  if file_ref && !target.source_build_phase.files_references.include?(file_ref)
    target.add_file_references([file_ref])
  end
end

# Add all source files
main_group.recursive_children.each do |child|
  next unless child.is_a?(Xcodeproj::Project::Object::PBXFileReference)
  next unless child.path.end_with?('.swift')
  
  if !target.source_build_phase.files_references.include?(child)
    target.add_file_references([child])
  end
end

project.save
puts "✅ Project file cleaned and rebuilt successfully!"
EOF

chmod +x clean_project.rb
ruby clean_project.rb

# Clean and rebuild
xcodebuild clean -scheme MyTune
xcodebuild -scheme MyTune -sdk iphonesimulator build
```

## Verification

After fixing, verify the build:

```bash
cd /Users/carlosalbertoruizrobles/Desarrollos/my-tune/ios
xcodebuild -scheme MyTune -sdk iphonesimulator build 2>&1 | grep -E "(BUILD|error:)"
```

**Expected Output**: `** BUILD SUCCEEDED **`

## Alternative: Start Fresh (Last Resort)

If the above doesn't work, you can regenerate the entire Xcode project:

```bash
cd /Users/carlosalbertoruizrobles/Desarrollos/my-tune/ios

# Backup
cp -r MyTune.xcodeproj MyTune.xcodeproj.backup

# Delete and regenerate
rm -rf MyTune.xcodeproj

# Use Xcode to create new project pointing to existing files
# File → New → Project → iOS App
# Choose "MyTune" as name
# Point to existing source files
# Add Accelerate framework in Build Phases
```

## After Fix is Complete

1. **Verify Build**
   ```bash
   xcodebuild -scheme MyTune -sdk iphonesimulator build
   ```

2. **Run in Simulator**
   ```bash
   xcodebuild -scheme MyTune -destination 'platform=iOS Simulator,name=iPhone 15' 
   ```

3. **Proceed with Manual Testing**
   - Follow `MANUAL_TESTING_US1.md` for complete test scenarios
   - Use physical device with real guitar for best results

## Need Help?

If issues persist:

1. Check that `xcodeproj` gem is installed:
   ```bash
   gem install xcodeproj
   ```

2. Verify all source files exist:
   ```bash
   find MyTune -name "*.swift" | sort
   ```

3. Check Xcode version:
   ```bash
   xcodebuild -version
   ```

Required: Xcode 14.0+ with Swift 5.9+

---

**Last Updated**: 2025-12-08  
**Related**: IOS_US1_COMPLETE.md, MANUAL_TESTING_US1.md
