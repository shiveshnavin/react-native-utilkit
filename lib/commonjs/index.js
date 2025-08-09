"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.startService = exports.sendEvent = exports.readAndUploadChunk = exports.pickFile = exports.multiply = exports.initEventBus = exports.hash = exports.download = exports.addListener = exports.UtilkitEvents = exports.Channels = void 0;
var _reactNative = require("react-native");
var Web = _interopRequireWildcard(require("./web.js"));
function _getRequireWildcardCache(e) { if ("function" != typeof WeakMap) return null; var r = new WeakMap(), t = new WeakMap(); return (_getRequireWildcardCache = function (e) { return e ? t : r; })(e); }
function _interopRequireWildcard(e, r) { if (!r && e && e.__esModule) return e; if (null === e || "object" != typeof e && "function" != typeof e) return { default: e }; var t = _getRequireWildcardCache(r); if (t && t.has(e)) return t.get(e); var n = { __proto__: null }, a = Object.defineProperty && Object.getOwnPropertyDescriptor; for (var u in e) if ("default" !== u && {}.hasOwnProperty.call(e, u)) { var i = a ? Object.getOwnPropertyDescriptor(e, u) : null; i && (i.get || i.set) ? Object.defineProperty(n, u, i) : n[u] = e[u]; } return n.default = e, t && t.set(e, n), n; }
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
if (_reactNative.Platform.OS != 'web') {
  const LINKING_ERROR = `The package 'react-native-utilkit' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
    ios: "- You have run 'pod install'\n",
    default: ''
  }) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
  _Utilkit = _reactNative.NativeModules.Utilkit ? _reactNative.NativeModules.Utilkit : new Proxy({}, {
    get() {
      throw new Error(LINKING_ERROR);
    }
  });
  EventManager = new _reactNative.NativeEventEmitter(_reactNative.NativeModules.Utilkit);
}
const Channels = exports.Channels = {
  Transfers: "Transfers",
  Generic: "Generic",
  Echo: "Echo"
};
const UtilkitEvents = exports.UtilkitEvents = EventManager;
const initEventBus = exports.initEventBus = _Utilkit.initEventBus;
const sendEvent = (channel, payload) => {
  //@ts-ignore
  return _Utilkit.sendEvent(channel, JSON.stringify(payload));
};
exports.sendEvent = sendEvent;
const addListener = (channel, callback) => {
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
exports.addListener = addListener;
const readAndUploadChunk = (uploadUrl, method, headers, multipartBody, bytesProcessed, totalBytes, chunkSize, file, onUploadProgress) => {
  if (_reactNative.Platform.OS == 'web') {
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
exports.readAndUploadChunk = readAndUploadChunk;
const download = (cloudFile, url, headers, provider, targetPath, listener, sourcePath) => {
  if (_reactNative.Platform.OS == 'web') return Web.download(cloudFile, url, headers, provider, targetPath, listener);

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
exports.download = download;
const hash = exports.hash = _Utilkit.hash;
const pickFile = mime => {
  if (_reactNative.Platform.OS == 'web') {
    return Web.pickFile(mime);
  }
  //@ts-ignore
  return _Utilkit.pickFile(mime).then(r => JSON.parse(r));
};
exports.pickFile = pickFile;
const multiply = exports.multiply = _Utilkit.multiply;
const startService = exports.startService = _Utilkit.startService;
//# sourceMappingURL=index.js.map