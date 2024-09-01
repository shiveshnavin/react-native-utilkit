import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import * as Web from './web'

let _Utilkit = {
  multiply: Web.multiply,
  startService: Web.startService,
  sendEvent: Web.sendEvent,
  initEventBus: Web.initEventBus,
  download: Web.download
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

export type EventListener = (payload: any | object) => void
export type EmitterSubscription = { remove: () => void }
export const UtilkitEvents = EventManager
export const initEventBus = _Utilkit.initEventBus
export const sendEvent = (channel: string, payload: object) => {
  //@ts-ignore
  return _Utilkit.sendEvent(channel, JSON.stringify(payload))
}
export const addListener = (channel: string, callback: EventListener): undefined | EmitterSubscription => {
  if (UtilkitEvents != undefined) {
    return UtilkitEvents.addListener(channel, (event: any) => {
      let payload = event.payload
      try {
        payload = JSON.parse(event.payload)
      } catch (e) { }
      callback(payload)
    })
  }
  return undefined
}


export const download = (
  cloudFile: Web.CloudFile,
  url: string,
  headers: any,
  provider: Web.CloudProvider,
  targetPath?: string,
  listener?: Web.FileOpListener,
  sourcePath?: string,
): Promise<Web.FileTransferResult> => {
  if (Platform.OS == 'web')
    return Web.download(cloudFile, url, headers, provider, targetPath, listener)

  /**
   *  fun download(cloudFile: String,
               url: String,
               headers: String,
               provider: String,
               targetPath: String,
               sourcePath: String,
               promise: Promise) 
   */
  //@ts-ignore
  return _Utilkit.download(JSON.stringify(cloudFile), url, JSON.stringify(headers), JSON.stringify(provider), targetPath, sourcePath).then(status => JSON.parse(status))
}


export const multiply = _Utilkit.multiply
export const startService = _Utilkit.startService