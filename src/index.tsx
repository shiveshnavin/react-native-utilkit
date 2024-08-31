import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

import * as Web from './index.web'

let _Utilkit = {
  multiply: Web.multiply,
  startService: Web.startService,
  sendEvent: Web.sendEvent,
  initEventBus: Web.initEventBus
}
let EventManager: NativeEventEmitter = {

} as any

if (Platform.OS != 'web') {
  const LINKING_ERROR =
    `The package 'react-native-utilkit' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

  _Utilkit = NativeModules.Utilkit
    ? NativeModules.Utilkit
    : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
  EventManager = new NativeEventEmitter(NativeModules.Utilkit);

}

export const Channels = {
  Transfers: "Transfers",
  Generic: "Generic",
  Echo: "Echo"
}

export const UtilkitEvents = EventManager

export const initEventBus = _Utilkit.initEventBus
export const sendEvent = _Utilkit.sendEvent
export const multiply = _Utilkit.multiply
export const startService = _Utilkit.startService