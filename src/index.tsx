import { NativeModules, Platform } from 'react-native';
import { multiply as multiplyWeb } from './index.web'

let Utilkit = {
  multiply: multiplyWeb
}

if (Platform.OS != 'web') {
  const LINKING_ERROR =
    `The package 'react-native-utilkit' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

  Utilkit = NativeModules.Utilkit
    ? NativeModules.Utilkit
    : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

}

export function multiply(a: number, b: number): Promise<number> {
  return Utilkit.multiply(a, b);
}