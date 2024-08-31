
export function multiply(a: number, b: number): Promise<number> {
  return Promise.resolve(a * b);
}

export function startService(payload: string): Promise<string> {
  throw new Error("startService Not implemented on web")
}

export function sendEvent(channel: string, payload: string): Promise<string> {
  throw new Error("sendEvent Not implemented on web")
}

export function initEventBus(): Promise<string> {
  throw new Error("initEventBus Not implemented on web")
}