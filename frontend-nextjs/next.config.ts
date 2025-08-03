import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'standalone', // Docker support
  experimental: {
    reactCompiler: true
  }
};

export default nextConfig;
