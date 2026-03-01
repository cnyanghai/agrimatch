import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.wogucloud.app',
  appName: '沃谷',
  webDir: 'dist',
  server: {
    // 生产环境使用本地资源
    hostname: 'localhost',
    androidScheme: 'https'
  }
};

export default config;
