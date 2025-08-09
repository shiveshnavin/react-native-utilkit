import { NativeEventEmitter } from 'react-native';
import * as Web from './web';
import type { PickedFile } from './web';
import type { AxiosProgressEvent } from 'axios';
export declare const Channels: {
    Transfers: string;
    Generic: string;
    Echo: string;
};
export type EventListener = (payload: any | object) => void;
export type EmitterSubscription = {
    remove: () => void;
};
export declare const UtilkitEvents: NativeEventEmitter;
export declare const initEventBus: typeof Web.initEventBus;
export declare const sendEvent: (channel: string, payload: object) => Promise<string>;
export declare const addListener: (channel: string, callback: EventListener) => undefined | EmitterSubscription;
export declare const readAndUploadChunk: (uploadUrl: string, method: string, headers: any, multipartBody: any, bytesProcessed: number, totalBytes: number, chunkSize: number, file: PickedFile, onUploadProgress: (progressEvent: AxiosProgressEvent) => void) => Promise<unknown>;
export declare const download: (cloudFile: Web.CloudFile, url: string, headers: any, provider: Web.CloudProvider, targetPath?: string, listener?: Web.FileOpListener, sourcePath?: string) => Promise<Web.FileTransferResult>;
export declare const hash: typeof Web.hash;
export declare const pickFile: (mime: string) => Promise<any>;
export declare const multiply: typeof Web.multiply;
export declare const startService: typeof Web.startService;
//# sourceMappingURL=index.d.ts.map