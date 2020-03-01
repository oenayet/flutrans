import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutrans/flutrans.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

final String clientID = "SB-Mid-client-6bYid__b2jLyX20n";
final String paymentURL = "https://dev-phoneix.recharge.id/recharge_backend_laravel_v2/public/v1.0.0/payment/topupcallback/";

class _MyAppState extends State<MyApp> {
  bool isMakePayment = false;
  final flutrans = Flutrans();
  @override
  void initState() {
    super.initState();
    flutrans.init(clientID,paymentURL);
    flutrans.setFinishCallback(_callback);
  }

  _makePayment() {
    setState(() {
      isMakePayment = true;
    });
    flutrans
        .makePayment(
          MidtransTransaction(
              7500,
              MidtransCustomer(
                  "Apin", "Prastya", "apin.klas@gmail.com", "085235419949"),
              [
                MidtransItem(
                  "5c18ea1256f67560cb6a00cdde3c3c7a81026c29",
                  7500,
                  2,
                  "USB FlashDisk",
                )
              ],
              skipCustomer: true,
              customField1: "ANYCUSTOMFIELD",
              //customField1: "DummyCustomField",
              saveCard: true,
              paymentMethod: 1,
              id: '423432'),
        )

        .catchError((err) => print("ERROR $err"));
  }

  Future<void> _callback(TransactionFinished finished) async {
    setState(() {
      isMakePayment = false;
    });
    return Future.value(null);
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Center(
          child: isMakePayment
              ? CircularProgressIndicator()
              : RaisedButton(
                  child: Text("Make Payment"),
                  onPressed: () => _makePayment(),
                ),
        ),
      ),
    );
  }
}
