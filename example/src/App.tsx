import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import * as Utilkit from 'react-native-utilkit';

export default function App() {
  const [result, setResult] = useState<number | string | undefined>();

  useEffect(() => {
    Utilkit.multiply(3, 7).then(setResult);
    Utilkit.initEventBus()
  }, []);

  useEffect(() => {
    const eventListener = Utilkit.UtilkitEvents.addListener(Utilkit.Channels.Transfers, (event: any) => {
      setResult(event.payload);
    });

    // Cleanup the event listener on component unmount
    return () => {
      eventListener.remove();
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
        const res = await (Utilkit.sendEvent(Utilkit.Channels.Transfers, JSON.stringify({ message: "TS says " + Date.now() })).catch(e => console.error(e)))
        setResult(res || 'err')
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
