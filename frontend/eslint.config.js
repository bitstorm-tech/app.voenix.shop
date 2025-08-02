
import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import reactThree from '@react-three/eslint-plugin';
import prettier from 'eslint-config-prettier';
import globals from 'globals';

export default [
  {
    ignores: ["dist/", "node_modules/", "build/", ".gradle/", "storage/", "*.config.js", "*.config.ts"],
  },
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['src/**/*.{js,jsx,ts,tsx}'],
    plugins: {
      react,
      'react-hooks': reactHooks,
      '@react-three': reactThree,
    },
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
      },
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    rules: {
      ...react.configs.recommended.rules,
      ...reactHooks.configs.recommended.rules,
      ...reactThree.configs.recommended.rules,
      'react/react-in-jsx-scope': 'off',
      'react/prop-types': 'off',
      // Allow Three.js properties in React Three Fiber components
      'react/no-unknown-property': [
        'error',
        {
          ignore: [
            // Three.js mesh/object properties
            'position',
            'rotation',
            'scale',
            'visible',
            'userData',
            'castShadow',
            'receiveShadow',
            // Three.js geometry properties
            'args',
            'attach',
            // Three.js material properties
            'map',
            'color',
            'metalness',
            'roughness',
            'envMapIntensity',
            'side',
            'transparent',
            'opacity',
            'emissive',
            'emissiveIntensity',
            // Three.js light properties
            'intensity',
            'angle',
            'penumbra',
            'decay',
            'distance',
            'target',
            // Three.js camera properties
            'fov',
            'aspect',
            'near',
            'far',
            // React Three Fiber specific
            'dispose',
            'raycast',
            'onClick',
            'onPointerOver',
            'onPointerOut',
            'onPointerDown',
            'onPointerUp',
            'onPointerMove',
            'onPointerEnter',
            'onPointerLeave',
            'onPointerMissed',
            'onUpdate',
            'onWheel',
            'onContextMenu',
            'onDoubleClick',
            // OrbitControls and other drei components
            'enablePan',
            'enableZoom',
            'enableRotate',
            'autoRotate',
            'autoRotateSpeed',
            'minDistance',
            'maxDistance',
            'minPolarAngle',
            'maxPolarAngle',
            'minAzimuthAngle',
            'maxAzimuthAngle',
            'enableDamping',
            'dampingFactor',
            'screenSpacePanning',
            'keyPanSpeed',
            'touches',
            'mouseButtons'
          ]
        }
      ],
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
  prettier,
];
