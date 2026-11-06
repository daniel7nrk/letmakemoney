const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function uid(name) {
  return crypto.createHash('md5').update(name, 'utf8').digest('hex').slice(0, 24).toUpperCase();
}

const iosDir = __dirname;
const promptpayDir = path.join(iosDir, 'PromptPay');

function walk(dir) {
  let results = [];
  const list = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of list) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      results = results.concat(walk(fullPath));
    } else {
      results.push(path.relative(iosDir, fullPath).replace(/\\/g, '/'));
    }
  }
  return results.sort();
}

const allFiles = walk(promptpayDir);
const swiftFiles = allFiles.filter(f => f.endsWith('.swift'));
const fontFiles = allFiles.filter(f => f.endsWith('.ttf'));
const plistFiles = allFiles.filter(f => f.endsWith('Info.plist'));

// IDs
const projId = uid('PromptPayProject');
const targetId = uid('PromptPayTarget');
const mainGroupId = uid('MainGroup');
const productsGroupId = uid('ProductsGroup');
const appProductId = uid('PromptPayAppProduct');
const sourcesPhaseId = uid('SourcesBuildPhase');
const frameworksPhaseId = uid('FrameworksBuildPhase');
const resourcesPhaseId = uid('ResourcesBuildPhase');
const projConfListId = uid('ProjConfigList');
const projDebugConfId = uid('ProjDebugConfig');
const projReleaseConfId = uid('ProjReleaseConfig');
const targetConfListId = uid('TargetConfigList');
const targetDebugConfId = uid('TargetDebugConfig');
const targetReleaseConfId = uid('TargetReleaseConfig');

const pbx = [];
pbx.push('// !$*UTF8*$!');
pbx.push('{');
pbx.push('\tarchiveVersion = 1;');
pbx.push('\tclasses = {');
pbx.push('\t};');
pbx.push('\tobjectVersion = 56;');
pbx.push('\tobjects = {');

// PBXBuildFile
pbx.push('\n/* Begin PBXBuildFile section */');
for (const f of swiftFiles) {
  pbx.push(`\t\t${uid('BF_' + f)} /* ${path.basename(f)} in Sources */ = {isa = PBXBuildFile; fileRef = ${uid('FR_' + f)} /* ${path.basename(f)} */; };`);
}
for (const f of fontFiles) {
  pbx.push(`\t\t${uid('BF_' + f)} /* ${path.basename(f)} in Resources */ = {isa = PBXBuildFile; fileRef = ${uid('FR_' + f)} /* ${path.basename(f)} */; };`);
}
pbx.push('/* End PBXBuildFile section */');

// PBXFileReference
pbx.push('\n/* Begin PBXFileReference section */');
pbx.push(`\t\t${appProductId} /* PromptPay.app */ = {isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = PromptPay.app; sourceTree = BUILT_PRODUCTS_DIR; };`);
for (const f of swiftFiles) {
  pbx.push(`\t\t${uid('FR_' + f)} /* ${path.basename(f)} */ = {isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = "${path.basename(f)}"; sourceTree = "<group>"; };`);
}
for (const f of fontFiles) {
  pbx.push(`\t\t${uid('FR_' + f)} /* ${path.basename(f)} */ = {isa = PBXFileReference; lastKnownFileType = file; path = "${path.basename(f)}"; sourceTree = "<group>"; };`);
}
for (const f of plistFiles) {
  pbx.push(`\t\t${uid('FR_' + f)} /* ${path.basename(f)} */ = {isa = PBXFileReference; lastKnownFileType = text.plist.xml; path = "${path.basename(f)}"; sourceTree = "<group>"; };`);
}
pbx.push('/* End PBXFileReference section */');

// PBXFrameworksBuildPhase
pbx.push('\n/* Begin PBXFrameworksBuildPhase section */');
pbx.push(`\t\t${frameworksPhaseId} /* Frameworks */ = {`);
pbx.push('\t\t\tisa = PBXFrameworksBuildPhase;');
pbx.push('\t\t\tbuildActionMask = 2147483647;');
pbx.push('\t\t\tfiles = (');
pbx.push('\t\t\t);');
pbx.push('\t\t\trunOnlyForDeploymentPostprocessing = 0;');
pbx.push('\t\t};');
pbx.push('/* End PBXFrameworksBuildPhase section */');

// Groups
const dirsList = new Set();
for (const f of allFiles) {
  dirsList.add(path.dirname(f).replace(/\\/g, '/'));
}
const sortedDirs = Array.from(dirsList).sort((a, b) => (b.split('/').length - a.split('/').length));

pbx.push('\n/* Begin PBXGroup section */');
for (const d of sortedDirs) {
  const gid = uid('GRP_' + d);
  const children = [];
  for (const f of allFiles) {
    if (path.dirname(f).replace(/\\/g, '/') === d) {
      children.push(`${uid('FR_' + f)} /* ${path.basename(f)} */`);
    }
  }
  for (const sd of dirsList) {
    if (path.dirname(sd).replace(/\\/g, '/') === d && sd !== d) {
      children.push(`${uid('GRP_' + sd)} /* ${path.basename(sd)} */`);
    }
  }

  const pathStr = d !== 'PromptPay' ? `path = "${path.basename(d)}"; ` : 'path = PromptPay; ';
  pbx.push(`\t\t${gid} /* ${path.basename(d)} */ = {`);
  pbx.push('\t\t\tisa = PBXGroup;');
  pbx.push('\t\t\tchildren = (');
  for (const c of children) {
    pbx.push(`\t\t\t\t${c},`);
  }
  pbx.push('\t\t\t);');
  pbx.push(`\t\t\t${pathStr}sourceTree = "<group>";`);
  pbx.push('\t\t};');
}

pbx.push(`\t\t${productsGroupId} /* Products */ = {`);
pbx.push('\t\t\tisa = PBXGroup;');
pbx.push('\t\t\tchildren = (');
pbx.push(`\t\t\t\t${appProductId} /* PromptPay.app */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t\tname = Products;');
pbx.push('\t\t\tsourceTree = "<group>";');
pbx.push('\t\t};');

pbx.push(`\t\t${mainGroupId} = {`);
pbx.push('\t\t\tisa = PBXGroup;');
pbx.push('\t\t\tchildren = (');
pbx.push(`\t\t\t\t${uid('GRP_PromptPay')} /* PromptPay */,`);
pbx.push(`\t\t\t\t${productsGroupId} /* Products */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t\tsourceTree = "<group>";');
pbx.push('\t\t};');
pbx.push('/* End PBXGroup section */');

// PBXNativeTarget
pbx.push('\n/* Begin PBXNativeTarget section */');
pbx.push(`\t\t${targetId} /* PromptPay */ = {`);
pbx.push('\t\t\tisa = PBXNativeTarget;');
pbx.push(`\t\t\tbuildConfigurationList = ${targetConfListId} /* Build configuration list for PBXNativeTarget "PromptPay" */;`);
pbx.push('\t\t\tbuildPhases = (');
pbx.push(`\t\t\t\t${sourcesPhaseId} /* Sources */,`);
pbx.push(`\t\t\t\t${frameworksPhaseId} /* Frameworks */,`);
pbx.push(`\t\t\t\t${resourcesPhaseId} /* Resources */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t\tbuildRules = (');
pbx.push('\t\t\t);');
pbx.push('\t\t\tdependencies = (');
pbx.push('\t\t\t);');
pbx.push('\t\t\tname = PromptPay;');
pbx.push('\t\t\tproductName = PromptPay;');
pbx.push(`\t\t\tproductReference = ${appProductId} /* PromptPay.app */;`);
pbx.push('\t\t\tproductType = "com.apple.product-type.application";');
pbx.push('\t\t};');
pbx.push('/* End PBXNativeTarget section */');

// PBXProject
pbx.push('\n/* Begin PBXProject section */');
pbx.push(`\t\t${projId} /* Project object */ = {`);
pbx.push('\t\t\tisa = PBXProject;');
pbx.push('\t\t\tattributes = {');
pbx.push('\t\t\t\tBuildIndependentTargetsInParallel = 1;');
pbx.push('\t\t\t\tLastUpgradeCheck = 1500;');
pbx.push('\t\t\t};');
pbx.push(`\t\t\tbuildConfigurationList = ${projConfListId} /* Build configuration list for PBXProject "PromptPay" */;`);
pbx.push('\t\t\tcompatibilityVersion = "Xcode 14.0";');
pbx.push('\t\t\tdevelopmentRegion = en;');
pbx.push('\t\t\thasScannedForEncodings = 0;');
pbx.push('\t\t\tknownRegions = (');
pbx.push('\t\t\t\ten,');
pbx.push('\t\t\t\tBase,');
pbx.push('\t\t\t);');
pbx.push(`\t\t\tmainGroup = ${mainGroupId};`);
pbx.push(`\t\t\tproductRefGroup = ${productsGroupId} /* Products */;`);
pbx.push('\t\t\tprojectDirPath = "";');
pbx.push('\t\t\tprojectRoot = "";');
pbx.push('\t\t\ttargets = (');
pbx.push(`\t\t\t\t${targetId} /* PromptPay */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t};');
pbx.push('/* End PBXProject section */');

// PBXResourcesBuildPhase
pbx.push('\n/* Begin PBXResourcesBuildPhase section */');
pbx.push(`\t\t${resourcesPhaseId} /* Resources */ = {`);
pbx.push('\t\t\tisa = PBXResourcesBuildPhase;');
pbx.push('\t\t\tbuildActionMask = 2147483647;');
pbx.push('\t\t\tfiles = (');
for (const f of fontFiles) {
  pbx.push(`\t\t\t\t${uid('BF_' + f)} /* ${path.basename(f)} in Resources */,`);
}
pbx.push('\t\t\t);');
pbx.push('\t\t\trunOnlyForDeploymentPostprocessing = 0;');
pbx.push('\t\t};');
pbx.push('/* End PBXResourcesBuildPhase section */');

// PBXSourcesBuildPhase
pbx.push('\n/* Begin PBXSourcesBuildPhase section */');
pbx.push(`\t\t${sourcesPhaseId} /* Sources */ = {`);
pbx.push('\t\t\tisa = PBXSourcesBuildPhase;');
pbx.push('\t\t\tbuildActionMask = 2147483647;');
pbx.push('\t\t\tfiles = (');
for (const f of swiftFiles) {
  pbx.push(`\t\t\t\t${uid('BF_' + f)} /* ${path.basename(f)} in Sources */,`);
}
pbx.push('\t\t\t);');
pbx.push('\t\t\trunOnlyForDeploymentPostprocessing = 0;');
pbx.push('\t\t};');
pbx.push('/* End PBXSourcesBuildPhase section */');

// XCBuildConfiguration
pbx.push('\n/* Begin XCBuildConfiguration section */');
pbx.push(`\t\t${projDebugConfId} /* Debug */ = {`);
pbx.push('\t\t\tisa = XCBuildConfiguration;');
pbx.push('\t\t\tbuildSettings = {');
pbx.push('\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;');
pbx.push('\t\t\t\tCLANG_ENABLE_MODULES = YES;');
pbx.push('\t\t\t\tCOPY_PHASE_STRIP = NO;');
pbx.push('\t\t\t\tDEBUG_INFORMATION_FORMAT = dwarf;');
pbx.push('\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;');
pbx.push('\t\t\t\tENABLE_TESTABILITY = YES;');
pbx.push('\t\t\t\tGCC_DYNAMIC_NO_PIC = NO;');
pbx.push('\t\t\t\tGCC_OPTIMIZATION_LEVEL = 0;');
pbx.push('\t\t\t\tGCC_PREPROCESSOR_DEFINITIONS = (');
pbx.push('\t\t\t\t\t"DEBUG=1",');
pbx.push('\t\t\t\t\t"$(inherited)",');
pbx.push('\t\t\t\t);');
pbx.push('\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;');
pbx.push('\t\t\t\tMTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;');
pbx.push('\t\t\t\tONLY_ACTIVE_ARCH = YES;');
pbx.push('\t\t\t\tSDKROOT = iphoneos;');
pbx.push('\t\t\t\tSWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;');
pbx.push('\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = "-Onone";');
pbx.push('\t\t\t};');
pbx.push('\t\t\tname = Debug;');
pbx.push('\t\t};');

pbx.push(`\t\t${projReleaseConfId} /* Release */ = {`);
pbx.push('\t\t\tisa = XCBuildConfiguration;');
pbx.push('\t\t\tbuildSettings = {');
pbx.push('\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;');
pbx.push('\t\t\t\tCLANG_ENABLE_MODULES = YES;');
pbx.push('\t\t\t\tCOPY_PHASE_STRIP = NO;');
pbx.push('\t\t\t\tDEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";');
pbx.push('\t\t\t\tENABLE_NS_ASSERTIONS = NO;');
pbx.push('\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;');
pbx.push('\t\t\t\tGCC_OPTIMIZATION_LEVEL = s;');
pbx.push('\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;');
pbx.push('\t\t\t\tMTL_ENABLE_DEBUG_INFO = NO;');
pbx.push('\t\t\t\tSDKROOT = iphoneos;');
pbx.push('\t\t\t\tSWIFT_COMPILATION_MODE = "wholemodule";');
pbx.push('\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = "-O";');
pbx.push('\t\t\t};');
pbx.push('\t\t\tname = Release;');
pbx.push('\t\t};');

pbx.push(`\t\t${targetDebugConfId} /* Debug */ = {`);
pbx.push('\t\t\tisa = XCBuildConfiguration;');
pbx.push('\t\t\tbuildSettings = {');
pbx.push('\t\t\t\tCODE_SIGNING_ALLOWED = NO;');
pbx.push('\t\t\t\tCODE_SIGN_IDENTITY = "";');
pbx.push('\t\t\t\tCODE_SIGNING_REQUIRED = NO;');
pbx.push('\t\t\t\tCURRENT_PROJECT_VERSION = 1;');
pbx.push('\t\t\t\tGENERATE_INFOPLIST_KEY_UILaunchScreen_Generation = YES;');
pbx.push('\t\t\t\tINFOPLIST_FILE = PromptPay/Info.plist;');
pbx.push('\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (');
pbx.push('\t\t\t\t\t"$(inherited)",');
pbx.push('\t\t\t\t\t"@executable_path/Frameworks",');
pbx.push('\t\t\t\t);');
pbx.push('\t\t\t\tMARKETING_VERSION = 1.0;');
pbx.push('\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = com.promptpay.app;');
pbx.push('\t\t\t\tPRODUCT_NAME = "$(TARGET_NAME)";');
pbx.push('\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;');
pbx.push('\t\t\t\tSWIFT_VERSION = 5.0;');
pbx.push('\t\t\t\tTARGETED_DEVICE_FAMILY = "1,2";');
pbx.push('\t\t\t};');
pbx.push('\t\t\tname = Debug;');
pbx.push('\t\t};');

pbx.push(`\t\t${targetReleaseConfId} /* Release */ = {`);
pbx.push('\t\t\tisa = XCBuildConfiguration;');
pbx.push('\t\t\tbuildSettings = {');
pbx.push('\t\t\t\tCODE_SIGNING_ALLOWED = NO;');
pbx.push('\t\t\t\tCODE_SIGN_IDENTITY = "";');
pbx.push('\t\t\t\tCODE_SIGNING_REQUIRED = NO;');
pbx.push('\t\t\t\tCURRENT_PROJECT_VERSION = 1;');
pbx.push('\t\t\t\tGENERATE_INFOPLIST_KEY_UILaunchScreen_Generation = YES;');
pbx.push('\t\t\t\tINFOPLIST_FILE = PromptPay/Info.plist;');
pbx.push('\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (');
pbx.push('\t\t\t\t\t"$(inherited)",');
pbx.push('\t\t\t\t\t"@executable_path/Frameworks",');
pbx.push('\t\t\t\t);');
pbx.push('\t\t\t\tMARKETING_VERSION = 1.0;');
pbx.push('\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = com.promptpay.app;');
pbx.push('\t\t\t\tPRODUCT_NAME = "$(TARGET_NAME)";');
pbx.push('\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;');
pbx.push('\t\t\t\tSWIFT_VERSION = 5.0;');
pbx.push('\t\t\t\tTARGETED_DEVICE_FAMILY = "1,2";');
pbx.push('\t\t\t};');
pbx.push('\t\t\tname = Release;');
pbx.push('\t\t};');
pbx.push('/* End XCBuildConfiguration section */');

// XCConfigurationList
pbx.push('\n/* Begin XCConfigurationList section */');
pbx.push(`\t\t${projConfListId} /* Build configuration list for PBXProject "PromptPay" */ = {`);
pbx.push('\t\t\tisa = XCConfigurationList;');
pbx.push('\t\t\tbuildConfigurations = (');
pbx.push(`\t\t\t\t${projDebugConfId} /* Debug */,`);
pbx.push(`\t\t\t\t${projReleaseConfId} /* Release */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t\tdefaultConfigurationIsVisible = 0;');
pbx.push('\t\t\tdefaultConfigurationName = Release;');
pbx.push('\t\t};');

pbx.push(`\t\t${targetConfListId} /* Build configuration list for PBXNativeTarget "PromptPay" */ = {`);
pbx.push('\t\t\tisa = XCConfigurationList;');
pbx.push('\t\t\tbuildConfigurations = (');
pbx.push(`\t\t\t\t${targetDebugConfId} /* Debug */,`);
pbx.push(`\t\t\t\t${targetReleaseConfId} /* Release */,`);
pbx.push('\t\t\t);');
pbx.push('\t\t\tdefaultConfigurationIsVisible = 0;');
pbx.push('\t\t\tdefaultConfigurationName = Release;');
pbx.push('\t\t};');
pbx.push('/* End XCConfigurationList section */');

pbx.push('\t};');
pbx.push(`\trootObject = ${projId} /* Project object */;`);
pbx.push('}');

const outDir = path.join(iosDir, 'PromptPay.xcodeproj');
fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, 'project.pbxproj'), pbx.join('\n') + '\n', 'utf8');

console.log('Generated ios/PromptPay.xcodeproj/project.pbxproj successfully!');
console.log(`Included ${swiftFiles.length} Swift files, ${fontFiles.length} font files, and ${plistFiles.length} plist files.`);
