import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import * as Utilkit from 'react-native-utilkit';

export default function App() {
  const [result, setResult] = useState<number | string | undefined>();

  useEffect(() => {
    Utilkit.multiply(3, 7).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button title='START' onPress={async () => {
        const res = await (Utilkit.startService('Lets go').catch(e => console.error(e)))
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
