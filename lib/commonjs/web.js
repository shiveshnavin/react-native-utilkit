"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.download = exports.Status = void 0;
exports.hash = hash;
exports.initEventBus = initEventBus;
exports.multiply = multiply;
exports.pickFile = pickFile;
exports.readAndUploadChunk = void 0;
exports.sendEvent = sendEvent;
exports.startService = startService;
//@ts-nocheck

function multiply(a, b) {
  return Promise.resolve(a * b);
}
function startService(payload) {
  throw new Error("startService Not implemented on web");
}
function sendEvent(channel, payload) {
  throw new Error("sendEvent Not implemented on web");
}
function initEventBus() {
  throw new Error("initEventBus Not implemented on web");
}
function pickFile(payload) {
  throw new Error("pickFile Not implemented on web");
}
function hash(filePath, algo) {
  throw new Error("hash Not implemented on web");
}
const download = function (cloudFile, url, headers, provider, targetPath, listener) {
  throw new Error("download Not implemented on web");
};
exports.download = download;
const readAndUploadChunk = (uploadUrl, method, headers, multipartBody, bytesProcessed, totalBytes, chunkSize, file, onUploadProgress) => {
  throw new Error("readFileChunk Not implemented on web");
};
exports.readAndUploadChunk = readAndUploadChunk;
let Status = exports.Status = /*#__PURE__*/function (Status) {
  Status["Active"] = "ACTIVE";
  Status["Failure"] = "FAILURE";
  Status["Inactive"] = "INACTIVE";
  Status["Success"] = "SUCCESS";
  return Status;
}({});
//# sourceMappingURL=web.js.map