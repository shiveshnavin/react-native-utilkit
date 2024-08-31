export function multiply(a: number, b: number): Promise<number> {
  return Promise.resolve(a * b);
}

export function startService(title: string): Promise<string> {
  throw new Error("startService Not implemented on web")
}