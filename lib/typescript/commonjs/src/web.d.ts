import type { AxiosProgressEvent, AxiosResponse } from "axios";
export declare function multiply(a: number, b: number): Promise<number>;
export declare function startService(payload: string): Promise<string>;
export declare function sendEvent(channel: string, payload: object): Promise<string>;
export declare function initEventBus(): Promise<string>;
export declare function pickFile(payload: string): Promise<PickedFile>;
export declare function hash(filePath: string, algo: string): Promise<string>;
export declare const download: (cloudFile: any, url: string, headers: any, provider: any, targetPath?: string, listener?: FileOpListener) => Promise<FileTransferResult>;
export declare const readAndUploadChunk: UploadChunkProxy;
export type UploadChunkProxy = (uploadUrl: string, method: string, headers: any, multipartBody?: any, bytesProcessed: number, totalBytes: number, chunkSize: number, file: PickedFile, onUploadProgress: (progressEvent: AxiosProgressEvent) => void) => Promise<AxiosResponse>;
export type PickedFile = {
    id: string;
    mimeType: string;
    name: string;
    size: number;
    uri: string;
    updated?: string;
    webFile?: any;
    reader: {
        getChunk: (offset: number, chunkSize: number) => Promise<Uint8Array>;
    };
};
export type FileOpListener = (update: FileTransferResult, cancle?: () => void) => void;
export type FileTransferResult = {
    transferType: 'download' | 'upload';
    provider: CloudProvider;
    id: string;
    statusMessage?: string;
    status: Status;
    bytesProcessed: number;
    totalBytes: number;
    localFilePath?: string;
    targetPath: string;
    sourcePath?: string;
    sourceProvider?: CloudProvider;
    targetRef?: string;
    cancel?: () => void;
};
export declare enum Status {
    Active = "ACTIVE",
    Failure = "FAILURE",
    Inactive = "INACTIVE",
    Success = "SUCCESS"
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
    ext: string;
    fileRefId: string;
    id: string;
    name: string;
    size: string;
};
//# sourceMappingURL=web.d.ts.map