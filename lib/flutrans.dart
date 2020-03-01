import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

typedef Future<void> MidtransCallback(TransactionFinished transactionFinished);

class Flutrans {
  MidtransCallback finishCallback;
  static Flutrans _instance = Flutrans._internal();
  static const MethodChannel _channel = const MethodChannel('flutrans');

  Flutrans._internal() {
    _channel.setMethodCallHandler(_channelHandler);
  }

  factory Flutrans() {
    return _instance;
  }

  Future<dynamic> _channelHandler(MethodCall methodCall) async {
    if (methodCall.method == "onTransactionFinished") {
      if (finishCallback != null) {

        print('onTransactionFinished start arguments=${methodCall.arguments} ' );

        TransactionFinished transactionFinished = new TransactionFinished();
        transactionFinished.transactionCanceled = methodCall.arguments['transactionCanceled'];
        transactionFinished.status = methodCall.arguments['status'];
        transactionFinished.source = methodCall.arguments['source'];
        transactionFinished.statusMessage = methodCall.arguments['statusMessage'];
        transactionFinished.orderId = methodCall.arguments['order_id'];
        transactionFinished.paymentType = methodCall.arguments['payment_type'];
        transactionFinished.paymentCode = methodCall.arguments['payment_code'];
        transactionFinished.bank = methodCall.arguments['bank'];
        transactionFinished.savedTokenId = methodCall.arguments['saved_token_id'];
        transactionFinished.transactionId = methodCall.arguments['transaction_id'];
        transactionFinished.transactionStatus = methodCall.arguments['transaction_status'];
        transactionFinished.transactionTime = methodCall.arguments['transaction_time'];
        print('onTransactionFinished before grossAmount');

        try {
          transactionFinished.grossAmount =
              double.parse(methodCall.arguments['gross_amount']);
        }
        catch (ex) {
          print(ex);
        }

        print('onTransactionFinished end');

        await finishCallback(transactionFinished);
      }
    }
    return Future.value(null);
  }

  void setFinishCallback(MidtransCallback callback) {
    finishCallback = callback;
  }

  Future<void> init(String clientId, String url,
      {String env = 'production'}) async {

    try {
      await _channel.invokeMethod("init", {
        "client_key": clientId,
        "base_url": url,
        "env": env,
      });
    }
    catch (ex) {
      print('error while invoking init ' + ex.toString());
      throw ex;
    }

    return Future.value(null);
  }

  Future<void> makePayment(MidtransTransaction transaction) async {
    /*int total = 0;
    transaction.items.forEach((v) => total += (v.price * v.quantity));
    if (total != transaction.total)
      throw "Transaction total and items total not equal";*/
    await _channel.invokeMethod("payment", jsonEncode(transaction.toJson()));
    return Future.value(null);
  }
}

class MidtransCustomer {
  final String firstName;
  final String lastName;
  final String email;
  final String phone;
  MidtransCustomer(this.firstName, this.lastName, this.email, this.phone);
  MidtransCustomer.fromJson(Map<String, dynamic> json)
      : firstName = json["first_name"],
        lastName = json["last_name"],
        email = json["email"],
        phone = json["phone"];
  Map<String, dynamic> toJson() {
    return {
      "first_name": firstName,
      "last_name": lastName,
      "email": email,
      "phone": phone,
    };
  }
}

class MidtransItem {
  final String id;
  final int price;
  final int quantity;
  final String name;
  MidtransItem(this.id, this.price, this.quantity, this.name);
  MidtransItem.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        price = json["price"],
        quantity = json["quantity"],
        name = json["name"];
  Map<String, dynamic> toJson() {
    return {
      "id": id,
      "price": price,
      "quantity": quantity,
      "name": name,
    };
  }
}

class MidtransTransaction {
  final int total;
  final MidtransCustomer customer;
  final List<MidtransItem> items;
  final bool skipCustomer;
  final bool saveCard;
  final String customField1;
  final String id;
  final String token;
  final int paymentMethod;

  MidtransTransaction(
    this.total,
    this.customer,
    this.items, {
    this.customField1,
    this.skipCustomer = false,
    this.saveCard = true,
    this.id = null,
        this.token = null,
        this.paymentMethod = -1,
  });
  Map<String, dynamic> toJson() {
    return {
      "id": id,
      "total": total,
      "skip_customer": skipCustomer,
      "save_card": saveCard,
      "items": items.map((v) => v.toJson()).toList(),
      "customer": customer.toJson(),
      "custom_field_1": customField1,
      "token": token,
      "payment_method": paymentMethod,
    };
  }
}

class TransactionFinished {
  bool transactionCanceled ;
  String status;
  String source;
  String statusMessage;
  String orderId;
  String paymentType;
  String paymentCode;
  String bank;
  String savedTokenId;
  String transactionId;
  String transactionStatus;
  String transactionTime;
  double grossAmount;
}
