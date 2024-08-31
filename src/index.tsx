import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import * as Web from './index.web'

let _Utilkit = {
  multiply: Web.multiply,
  startService: Web.startService,
  sendEvent: Web.sendEvent,
  initEventBus: Web.initEventBus
}
//@ts-ignore
let EventManager: NativeEventEmitter = undefined

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

export type EventListener = (channel: string, payload: object) => void
export type EmitterSubscription = { remove: () => void }
export const UtilkitEvents = EventManager
export const initEventBus = _Utilkit.initEventBus
export const sendEvent = _Utilkit.sendEvent
export const addListener = (channel: string, callback: EventListener): undefined | EmitterSubscription => {
  if (EventManager != undefined) {
    return EventManager.addListener(channel, (event: any) => {
      callback(event.channel, event.payload)
    })
  }
  return undefined
}


export const multiply = _Utilkit.multiply
export const startService = _Utilkit.startService