"use strict";

import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import * as Web from './web';
let _Utilkit = {
  multiply: Web.multiply,
  startService: Web.startService,
  sendEvent: Web.sendEvent,
  initEventBus: Web.initEventBus,
  download: Web.download,
  readAndUploadChunk: Web.readAndUploadChunk,
  pickFile: Web.pickFile,
  hash: Web.hash
};
//@ts-ignore
let EventManager = undefined;
if (Platform.OS != 'web') {
  const LINKING_ERROR = `The package 'react-native-utilkit' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
    ios: "- You have run 'pod install'\n",
    default: ''
  }) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
  _Utilkit = NativeModules.Utilkit ? NativeModules.Utilkit : new Proxy({}, {
    get() {
      throw new Error(LINKING_ERROR);
    }
  });
  EventManager = new NativeEventEmitter(NativeModules.Utilkit);
}
export const Channels = {
  Transfers: "Transfers",
  Generic: "Generic",
  Echo: "Echo"
};
export const UtilkitEvents = EventManager;
export const initEventBus = _Utilkit.initEventBus;
export const sendEvent = (channel, payload) => {
  //@ts-ignore
  return _Utilkit.sendEvent(channel, JSON.stringify(payload));
};
export const addListener = (channel, callback) => {
  if (UtilkitEvents != undefined) {
    return UtilkitEvents.addListener(channel, event => {
      let payload = event.payload;
      try {
        payload = JSON.parse(event.payload);
      } catch (e) {}
      callback(payload);
    });
  }
  return undefined;
};
export const readAndUploadChunk = (uploadUrl, method, headers, multipartBody, bytesProcessed, totalBytes, chunkSize, file, onUploadProgress) => {
  if (Platform.OS == 'web') {
    return _Utilkit.readAndUploadChunk(uploadUrl, method, headers, multipartBody, bytesProcessed, totalBytes, chunkSize, file, onUploadProgress);
  }
  return new Promise((resolve, reject) => {
    //@ts-ignore
    _Utilkit.readAndUploadChunk(uploadUrl, method, JSON.stringify(headers), multipartBody == undefined ? "" : JSON.stringify(multipartBody), bytesProcessed, totalBytes, chunkSize, JSON.stringify(file)).then(response => {
      onUploadProgress({
        loaded: Math.min(chunkSize, totalBytes - bytesProcessed),
        bytes: Math.min(bytesProcessed + chunkSize - 1, totalBytes),
        lengthComputable: true
      });
      try {
        response = JSON.parse(response);
        response.data = JSON.parse(response.data);
      } catch (e) {}
      if (response.status >= 400) {
        reject(response);
      } else {
        resolve(response);
      }
    });
  });
};
export const download = (cloudFile, url, headers, provider, targetPath, listener, sourcePath) => {
  if (Platform.OS == 'web') return Web.download(cloudFile, url, headers, provider, targetPath, listener);

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
  return _Utilkit.download(JSON.stringify(cloudFile), url, JSON.stringify(headers), JSON.stringify(provider), targetPath, sourcePath).then(status => JSON.parse(status));
};
export const hash = _Utilkit.hash;
export const pickFile = mime => {
  if (Platform.OS == 'web') {
    return Web.pickFile(mime);
  }
  //@ts-ignore
  return _Utilkit.pickFile(mime).then(r => JSON.parse(r));
};
export const multiply = _Utilkit.multiply;
export const startService = _Utilkit.startService;
//# sourceMappingURL=index.js.map