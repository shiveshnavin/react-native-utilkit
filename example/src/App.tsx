import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import * as Utilkit from 'react-native-utilkit';
import type { CloudProvider, FileTransferResult } from '../../src/web';

export default function App() {
  const [result, setResult] = useState<number | string | undefined>();

  useEffect(() => {
    Utilkit.multiply(3, 7).then(setResult);
    Utilkit.initEventBus()
  }, []);

  useEffect(() => {
    const eventListener = Utilkit.addListener(Utilkit.Channels.Transfers, (payloads: FileTransferResult[]) => {
      let pl = payloads?.map(pl => {
        return {
          name: pl.id,
          status: pl.status,
          progress: `${(100 * pl.bytesProcessed / pl.totalBytes).toFixed(2)}%`
        }
      })
      if (pl) {
        setResult(JSON.stringify(pl));
      }

    });
    return () => {
      eventListener?.remove();
    };
  }, []);
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button title='START' onPress={async () => {
        const res = await (Utilkit.startService('Lets go').catch(e => console.error(e)))
        setResult(res || 'err')
      }}></Button>
      <View style={{
        padding: 10
      }} />
      <Button title='Send' onPress={async () => {
        const res = await (Utilkit.sendEvent(Utilkit.Channels.Transfers, { message: "TS says " + Date.now() }).catch(e => console.error(e)))
        setResult(res || 'err')
      }}></Button>
      <View style={{
        padding: 10
      }} />
      <Button title='Download' onPress={async () => {
        const res: FileTransferResult = await Utilkit.download(
          {
            ext: 'application/zip',
            id: 'abc_' + Date.now(),
            name: Date.now() + '_.avi',
            size: `${18 * 1024 * 1024}`,
            fileRefId: 'abcd1234'
          },
          'https://www.quintic.com/software/sample_videos/Equine%20Walk%20400fps.avi',
          {},
          {} as CloudProvider,
          "",
          undefined,
          "storage/512MB.zip"
        )
        setResult(res.status)
      }}></Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
