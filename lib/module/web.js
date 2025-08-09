"use strict";

//@ts-nocheck

export function multiply(a, b) {
  return Promise.resolve(a * b);
}
export function startService(payload) {
  throw new Error("startService Not implemented on web");
}
export function sendEvent(channel, payload) {
  throw new Error("sendEvent Not implemented on web");
}
export function initEventBus() {
  throw new Error("initEventBus Not implemented on web");
}
export function pickFile(payload) {
  throw new Error("pickFile Not implemented on web");
}
export function hash(filePath, algo) {
  throw new Error("hash Not implemented on web");
}
export const download = function (cloudFile, url, headers, provider, targetPath, listener) {
  throw new Error("download Not implemented on web");
};
export const readAndUploadChunk = (uploadUrl, method, headers, multipartBody, bytesProcessed, totalBytes, chunkSize, file, onUploadProgress) => {
  throw new Error("readFileChunk Not implemented on web");
};
export let Status = /*#__PURE__*/function (Status) {
  Status["Active"] = "ACTIVE";
  Status["Failure"] = "FAILURE";
  Status["Inactive"] = "INACTIVE";
  Status["Success"] = "SUCCESS";
  return Status;
}({});
//# sourceMappingURL=web.js.map