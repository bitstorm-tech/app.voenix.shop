import { getCroppedImgFromArea } from '@/lib/imageCropUtils';
import { Environment, OrbitControls } from '@react-three/drei';
import { Canvas, useFrame, useLoader } from '@react-three/fiber';
import { Suspense, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import * as THREE from 'three';
import { GeneratedImageCropData, MugOption } from '../../types';

interface MugPreview3DProps {
  mug: MugOption;
  imageUrl: string;
  cropData: GeneratedImageCropData | null;
}

interface MugModelProps {
  textureUrl: string;
}

function MugModel({ textureUrl }: MugModelProps) {
  const meshRef = useRef<THREE.Group>(null);
  const texture = useLoader(THREE.TextureLoader, textureUrl);

  // Gentle rotation animation
  useFrame((_, delta) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += delta * 0.1;
    }
  });

  // Create mug geometry with more realistic proportions
  const mugRadiusTop = 1.4;
  const mugRadiusBottom = 1.4;
  const mugHeight = 3.2;
  const handleRadius = 0.15;
  const handleSize = 0.9;
  const handleOffset = -0.5;
  const wallThickness = 0.08;

  return (
    <group ref={meshRef}>
      {/* Mug body - slightly tapered with open top */}
      <mesh position={[0, 0, 0]}>
        <cylinderGeometry args={[mugRadiusTop, mugRadiusBottom, mugHeight, 64, 1, true]} />
        <meshStandardMaterial map={texture} metalness={0.05} roughness={0.4} envMapIntensity={0.5} side={THREE.DoubleSide} />
      </mesh>

      {/* Inner wall - creates the hollow interior */}
      <mesh position={[0, wallThickness / 2, 0]}>
        <cylinderGeometry args={[mugRadiusTop - wallThickness, mugRadiusBottom - wallThickness, mugHeight - wallThickness, 64, 1, true]} />
        <meshStandardMaterial color="#f5f5f5" metalness={0.05} roughness={0.9} side={THREE.DoubleSide} />
      </mesh>

      {/* Mug handle - complete C-shaped handle */}
      <group position={[mugRadiusTop + handleSize / 2 + handleOffset, 0, 0]} rotation={[0, 0, Math.PI * 1.5]}>
        <mesh>
          <torusGeometry args={[handleSize * 0.8, handleRadius, 12, 24, Math.PI]} />
          <meshStandardMaterial color="#ffffff" metalness={0.1} roughness={0.6} />
        </mesh>
      </group>

      {/* Mug bottom - solid base */}
      <mesh position={[0, -mugHeight / 2 - 0.1, 0]}>
        <cylinderGeometry args={[mugRadiusBottom * 0.98, mugRadiusBottom * 0.98, 0.2, 64]} />
        <meshStandardMaterial color="#ffffff" metalness={0.05} roughness={0.8} />
      </mesh>

      {/* Bottom inner disk - closes the interior */}
      <mesh position={[0, -mugHeight / 2 + wallThickness + 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <circleGeometry args={[mugRadiusBottom - wallThickness, 64]} />
        <meshStandardMaterial color="#f5f5f5" metalness={0.05} roughness={0.9} />
      </mesh>

      {/* Rim - rounded edge at the top */}
      <mesh position={[0, mugHeight / 2, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <torusGeometry args={[mugRadiusTop - wallThickness / 2, wallThickness / 2, 8, 32]} />
        <meshStandardMaterial color="#ffffff" metalness={0.1} roughness={0.6} />
      </mesh>
    </group>
  );
}

function LoadingSpinner() {
  return (
    <div className="flex h-full items-center justify-center">
      <div className="border-t-primary h-12 w-12 animate-spin rounded-full border-4 border-gray-300"></div>
    </div>
  );
}

export default function MugPreview3D({ imageUrl, cropData }: MugPreview3DProps) {
  const [processedImageUrl, setProcessedImageUrl] = useState<string | null>(null);
  const [isProcessing, setIsProcessing] = useState(true);
  const { t } = useTranslation('editor');

  useEffect(() => {
    const processImage = async () => {
      setIsProcessing(true);
      try {
        if (cropData?.croppedAreaPixels) {
          const croppedImage = await getCroppedImgFromArea(imageUrl, cropData.croppedAreaPixels);
          setProcessedImageUrl(croppedImage);
        } else {
          setProcessedImageUrl(imageUrl);
        }
      } catch (error) {
        console.error('Error processing image:', error);
        setProcessedImageUrl(imageUrl);
      } finally {
        setIsProcessing(false);
      }
    };

    processImage();
  }, [imageUrl, cropData]);

  if (isProcessing || !processedImageUrl) {
    return (
      <div className="h-[400px] w-full">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="h-[400px] w-full rounded-lg bg-gradient-to-br from-gray-100 to-gray-200 shadow-inner">
      <Canvas camera={{ position: [5, 2, 5], fov: 40 }}>
        <OrbitControls
          enablePan={false}
          enableZoom={true}
          minDistance={5}
          maxDistance={10}
          minPolarAngle={Math.PI / 6}
          maxPolarAngle={Math.PI / 2.2}
          autoRotate
          autoRotateSpeed={0.5}
          target={[0, -0.5, 0]}
        />

        {/* Lighting */}
        <ambientLight intensity={0.5} />
        <directionalLight position={[5, 5, 5]} intensity={1} />
        <directionalLight position={[-5, 3, -5]} intensity={0.3} />

        {/* Environment for reflections */}
        <Environment preset="studio" />

        {/* Mug Model */}
        <Suspense fallback={null}>
          <MugModel textureUrl={processedImageUrl} />
        </Suspense>
      </Canvas>

      {/* Instructions */}
      <div className="mt-2 text-center text-sm text-gray-600">{t('steps.preview.controlsHint')}</div>
    </div>
  );
}
