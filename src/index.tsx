import { NativeModules, Platform } from 'react-native';
import * as Web from './index.web'

let Utilkit = {
  multiply: Web.multiply,
  startService: Web.startService
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


export function startService(title: string): Promise<string> {
  return Utilkit.startService(title);
}