package com.lekapin.flutrans.flutrans;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.UIKitCustomSetting;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
//OKE:
import com.midtrans.sdk.corekit.core.PaymentMethod;
import com.midtrans.sdk.corekit.models.snap.Authentication;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
///

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutransPlugin */
public class FlutransPlugin implements MethodCallHandler, TransactionFinishedCallback {
  static final String TAG = "FlutransPlugin";
  private final Registrar registrar;
  private final MethodChannel channel;
  private Context context;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutrans");
    channel.setMethodCallHandler(new FlutransPlugin(registrar, channel));
  }

  private FlutransPlugin(Registrar registrar, MethodChannel channel) {
    this.registrar = registrar;
    this.channel = channel;
    this.context = registrar.activeContext();
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {

    if(call.method.equals("init")) {
      initMidtransSdk((String)call.argument("client_key").toString(), call.argument("base_url").toString());
    } else if(call.method.equals("payment")) {
      String str = call.arguments();
      try {
        payment(str);
      }
      catch (Exception ex) {
          result.error("",ex.getMessage(),null);
      }
    } else {
      result.notImplemented();
    }
  }

  private void initMidtransSdk(String client_key, String base_url) {
    SdkUIFlowBuilder.init()
            .setClientKey(client_key) // client_key is mandatory
            .setContext(context) // context is mandatory
            .setTransactionFinishedCallback(this) // set transaction finish callback (sdk callback)
            .setMerchantBaseUrl(base_url) //set merchant url
            .enableLog(true) // enable sdk logcom.lekapin.flutrans.flutrans.FlutransPlugin#4CAF50", "#009688", "#CDDC39")) // will replace theme on snap theme on MAP
            .buildSDK();

  }

  void payment(String str) throws Exception {

        Log.d(TAG, str);
      JSONObject json = new JSONObject(str);
      JSONObject cJson = json.getJSONObject("customer");

      String id = null;
      if(json.has("id") && json.getString("id") != null)
        id = json.getString("id");
      if (id == null)
        id = System.currentTimeMillis() + "";

      //OKE: Used id
      TransactionRequest transactionRequest = new
              TransactionRequest(id, json.getInt("total"));

      ArrayList<ItemDetails> itemList = new ArrayList<>();
      JSONArray arr = json.getJSONArray("items");
      for(int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);
        ItemDetails item = new ItemDetails(obj.getString("id"), obj.getInt("price"), obj.getInt("quantity"), obj.getString("name"));
        itemList.add(item);
      }
      CustomerDetails cus = new CustomerDetails();

      cus.setFirstName(cJson.getString("first_name"));
        cus.setLastName(cJson.getString("last_name"));
        cus.setEmail(cJson.getString("email"));
        cus.setPhone(cJson.getString("phone"));
      transactionRequest.setCustomerDetails(cus);
      if(json.has("custom_field_1"))
        transactionRequest.setCustomField1(json.getString("custom_field_1"));
      transactionRequest.setItemDetails(itemList);
      UIKitCustomSetting setting = MidtransSDK.getInstance().getUIKitCustomSetting();
      if(json.has("skip_customer"))
        setting.setSkipCustomerDetailsPages(json.getBoolean("skip_customer"));
      MidtransSDK.getInstance().setUIKitCustomSetting(setting);

      CreditCard creditCardOptions = new CreditCard();
      if(json.has("save_card"))
        creditCardOptions.setSaveCard(json.getBoolean("save_card"));
      creditCardOptions.setAuthentication(Authentication.AUTH_3DS);
      transactionRequest.setCreditCard(creditCardOptions);

      MidtransSDK.getInstance().setTransactionRequest(transactionRequest);


      String token = null;
      if(json.has("token"))
        token = json.getString("token");
      int paymentMethod = -1;
      if(json.has("payment_method"))
        paymentMethod = json.getInt("payment_method");

      if (token != null && paymentMethod != -1) {
          System.out.println("Starting payment Ui with token " + token);
          MidtransSDK.getInstance().startPaymentUiFlow(context, getPaymentMethod(paymentMethod), token);
      }
      else {
          System.out.println("Starting payment Ui without token");
          MidtransSDK.getInstance().startPaymentUiFlow(context);
      }


  }

  PaymentMethod getPaymentMethod(int method) throws Exception {

    if (method == 1) return PaymentMethod.GO_PAY;
    else if (method == 2 ) return PaymentMethod.BANK_TRANSFER_BCA;
    else if (method == 3 ) return PaymentMethod.BANK_TRANSFER_MANDIRI;
    else if (method == 4 ) return PaymentMethod.BANK_TRANSFER_BNI;
    else if (method == 5 ) return PaymentMethod.BANK_TRANSFER_PERMATA;
    else if (method == 6 ) return PaymentMethod.BANK_TRANSFER_OTHER;
    else if (method == 11 ) return PaymentMethod.CREDIT_CARD;

    throw new Exception("Payment method not found");
  }

  @Override
  public void onTransactionFinished(TransactionResult transactionResult) {

      Map<String, Object> content = new HashMap<>();
      content.put("transactionCanceled", transactionResult.isTransactionCanceled());
      content.put("status", transactionResult.getStatus());
      content.put("source", transactionResult.getSource());
      content.put("statusMessage", transactionResult.getStatusMessage());
      if(transactionResult.getResponse() != null) {
          content.put("order_id", transactionResult.getResponse().getOrderId());
          content.put("payment_type", transactionResult.getResponse().getPaymentType());
          content.put("payment_code", transactionResult.getResponse().getPaymentCode());
          content.put("bank", transactionResult.getResponse().getBank());
          content.put("saved_token_id", transactionResult.getResponse().getSavedTokenId());
          content.put("transaction_id", transactionResult.getResponse().getTransactionId());
          content.put("transaction_status", transactionResult.getResponse().getTransactionStatus());
          content.put("transaction_time", transactionResult.getResponse().getTransactionTime());
          content.put("gross_amount", transactionResult.getResponse().getGrossAmount());
      }
      else {
          content.put("order_id", null);
          content.put("payment_type", null);
          content.put("payment_code", null);
          content.put("bank", null);
          content.put("saved_token_id", null);
          content.put("transaction_id", null);
          content.put("transaction_status",null);
          content.put("transaction_time",null);
          content.put("gross_amount", null);
      }

      channel.invokeMethod("onTransactionFinished", content);
  }
}
