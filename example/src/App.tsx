import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import * as Utilkit from 'react-native-utilkit';
import type { CloudProvider, FileTransferResult } from '../../src/web';
import * as DocumentPicker from 'expo-document-picker';

export default function App() {
  const [result, setResult] = useState<number | string | undefined>();
  const [counter, setCounter] = useState(0)
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
      <Button title={`${counter}`} onPress={async () => {
        setCounter(counter + 1)
      }}></Button>
      <View style={{ padding: 10 }} />

      <Text>Result: {result}</Text>
      <Button title='START' onPress={async () => {
        const res = await (Utilkit.startService('Lets go').catch(e => console.error(e)))
        setResult(res || 'err')
      }}></Button>
      <View style={{ padding: 10 }} />
      <Button title='Send' onPress={async () => {
        const res = await (Utilkit.sendEvent(Utilkit.Channels.Transfers, { message: "TS says " + Date.now() }).catch(e => console.error(e)))
        setResult(res || 'err')
      }}></Button>
      <View style={{ padding: 10 }} />
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



      <View style={{ padding: 10 }} />
      <Button title='Read file' onPress={
        async () => {
          /**
           * 
 content://com.android.providers.media.documents/document/video%3A1000068137
https://content.dropboxapi.com/2/files/upload_session/finish content://com.android.providers.media.documents/document/document%3A1000068061

 content://com.android.providers.downloads.documents/document/msf%3A1000068118













https://content.dropboxapi.com/2/files/upload_session/finish file:///data/user/0/com.mypaperdrive.app/cache/ImagePicker/fa8d6b64-f499-4a3b-8fd2-c6de901655db.jpeg
 content://com.android.providers.downloads.documents/document/msf%3A1000068137

           */
          let chunkSize = 1 * 1024 * 1024
          let readed: string = await new Promise((resolve, reject) => {
            Utilkit.readAndUploadChunk('https://upload.box.com/api/2.0/files/content?fields=id',
              "post",
              {
                "authorization": `Bearer na8gesbL2uAuVvJK72lqXAXp0wYO0nrE`,
                "content-md5": "f649f73d51bc965d16534e8d69580a2e1f2b2940"
              },
              {
                "attributes": `{\"name\":\"space_out.mp4\",\"parent\":{\"id\":\"281281698815\"},\"content_created_at\":\"2024-12-12T10:53:43-08:00\",\"content_modified_at\":\"2024-12-12T10:53:43-08:00\"}`,
                "file": "@file"
              },
              0,
              978355,
              chunkSize,
              {
                uri: '/storage/emulated/0/Download/space_out.mp4',
                name: 'space_out.mp4',
                size: 978355,
                id: '__test__',
                mimeType: 'video/avi',
                reader: {
                  getChunk() {
                    return [] as any
                  }
                }
              },
              (e) => {
                setResult(JSON.stringify(e))
              }
            ).then(r => {
              setResult(JSON.stringify(r))
            }).catch((e) => {
              setResult(JSON.stringify(e))
            })
          })
        }
      }></Button>
    </View >
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
