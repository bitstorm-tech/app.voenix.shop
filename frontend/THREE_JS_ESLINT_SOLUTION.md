# Three.js ESLint Configuration Solution

## Problem
ESLint's `react/no-unknown-property` rule was flagging valid Three.js properties like `position`, `args`, `map`, `metalness`, `roughness`, `envMapIntensity`, `side`, `intensity`, and `rotation` as unknown properties in React Three Fiber components.

## Solution
Configured ESLint to ignore Three.js-specific properties by updating the `react/no-unknown-property` rule in `eslint.config.js`.

### Changes Made

1. **Installed @react-three/eslint-plugin** (provides performance optimization rules)
2. **Updated ESLint configuration** to ignore Three.js properties

### ESLint Configuration
```javascript
'react/no-unknown-property': [
  'error',
  {
    ignore: [
      // Three.js mesh/object properties
      'position', 'rotation', 'scale', 'visible', 'userData', 'castShadow', 'receiveShadow',
      // Three.js geometry properties
      'args', 'attach',
      // Three.js material properties
      'map', 'color', 'metalness', 'roughness', 'envMapIntensity', 'side', 'transparent', 'opacity', 'emissive', 'emissiveIntensity',
      // Three.js light properties
      'intensity', 'angle', 'penumbra', 'decay', 'distance', 'target',
      // Three.js camera properties
      'fov', 'aspect', 'near', 'far',
      // React Three Fiber specific
      'dispose', 'raycast', 'onClick', 'onPointerOver', 'onPointerOut', 'onPointerDown', 'onPointerUp', 'onPointerMove', 'onPointerEnter', 'onPointerLeave', 'onPointerMissed', 'onUpdate', 'onWheel', 'onContextMenu', 'onDoubleClick',
      // OrbitControls and other drei components
      'enablePan', 'enableZoom', 'enableRotate', 'autoRotate', 'autoRotateSpeed', 'minDistance', 'maxDistance', 'minPolarAngle', 'maxPolarAngle', 'minAzimuthAngle', 'maxAzimuthAngle', 'enableDamping', 'dampingFactor', 'screenSpacePanning', 'keyPanSpeed', 'touches', 'mouseButtons'
    ]
  }
],
```

## Result
- ✅ All `react/no-unknown-property` errors for Three.js properties are resolved
- ✅ TypeScript types remain intact and functional
- ✅ React Three Fiber components can use Three.js properties without ESLint errors
- ✅ Performance optimization rules from @react-three/eslint-plugin are enabled

## Files Modified
- `/frontend/eslint.config.js` - Updated ESLint configuration
- `/frontend/package.json` - Added @react-three/eslint-plugin dependency

## Testing
Run `npx eslint src/components/editor/components/shared/MugPreview3D.tsx` to verify no errors.