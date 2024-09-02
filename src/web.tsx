//@ts-nocheck
import type { AxiosProgressEvent, AxiosResponse } from "axios";

export function multiply(a: number, b: number): Promise<number> {
  return Promise.resolve(a * b);
}

export function startService(payload: string): Promise<string> {
  throw new Error("startService Not implemented on web")
}

export function sendEvent(channel: string, payload: object): Promise<string> {
  throw new Error("sendEvent Not implemented on web")
}

export function initEventBus(): Promise<string> {
  throw new Error("initEventBus Not implemented on web")
}

export function pickFile(payload: string): Promise<PickedFile> {
  throw new Error("pickFile Not implemented on web")
}

export const download = function (
  cloudFile: any,
  url: string,
  headers: any,
  provider: any,
  targetPath?: string,
  listener?: FileOpListener): Promise<FileTransferResult> {
  throw new Error("download Not implemented on web")
}

type Base64String = string
export const readAndUploadChunk: UploadChunkProxy = (
  uploadUrl: string,
  method: string,
  headers: any,
  bytesProcessed: number,
  totalBytes: number,
  chunkSize: number,
  file: PickedFile,
  onUploadProgress: (progressEvent: AxiosProgressEvent) => void) => {
  throw new Error("readFileChunk Not implemented on web")
}



export type UploadChunkProxy = (
  uploadUrl: string,
  method: string,
  headers: any,
  bytesProcessed: number,
  totalBytes: number,
  chunkSize: number,
  file: PickedFile,
  onUploadProgress: (progressEvent: AxiosProgressEvent) => void) => Promise<AxiosResponse>


export type PickedFile = {
  id: string,
  mimeType: string,
  name: string,
  size: number,
  uri: string, // File Uri or Base64 Uri
  updated?: string
  webFile?: any
  reader: {
    getChunk: (offset: number, chunkSize: number) => Promise<Uint8Array>
  }
}

export type FileOpListener = (update: FileTransferResult, cancle?: () => void) => void

export type FileTransferResult = {
  transferType: 'download' | 'upload'
  provider: CloudProvider,
  id: string
  statusMessage?: string
  status: Status,
  bytesProcessed: number
  totalBytes: number
  localFilePath?: string
  targetPath: string

  sourcePath?: string
  sourceProvider?: CloudProvider,

  targetRef?: string
  cancel?: () => void
}

export enum Status {
  Active = 'ACTIVE',
  Failure = 'FAILURE',
  Inactive = 'INACTIVE',
  Success = 'SUCCESS'
}

export type CloudProvider = {
  __typename?: string;
  access_token?: string;
  created_ts?: number;
  creds?: string;
  expires_ts?: number;
  id: string;
  name: string;
  provider: string;
  refresh_token?: string;
  status?: Status;
  userid: string;
};

export type CloudFile = {
  ext: string,
  fileRefId: string,
  id: string,
  name: string,
  size: string
}