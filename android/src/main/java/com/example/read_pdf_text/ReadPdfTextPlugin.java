package com.example.read_pdf_text;

import androidx.annotation.NonNull;
import android.os.AsyncTask;
import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import android.util.Log;



/** ReadPdfTextPlugin */
public class ReadPdfTextPlugin implements FlutterPlugin, MethodCallHandler {


  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
  
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "read_pdf_text");

    channel.setMethodCallHandler(new ReadPdfTextPlugin());
        // Getting the application context for pdfBox.
    PDFBoxResourceLoader.init(flutterPluginBinding.getApplicationContext());
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {

    final MethodChannel channel = new MethodChannel(registrar.messenger(), "read_pdf_text");
    channel.setMethodCallHandler(new ReadPdfTextPlugin());
      // Getting the application context for pdfBox.
    PDFBoxResourceLoader.init(registrar.activity().getApplicationContext());
  }


 static Result res;
  // Calls [parsePDFtext] when getting MethodCall
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        // Getting reference to the [result] so we can return the strings from pdf file.
        res = result;

    if (call.method.equals("getPDFtext")) {
        final String path = call.argument("path");
        parsePDFtext(path);
      } 
    else if(call.method.equals("getPDFtextPaginated")) {
        final String path = call.argument("path");
        paginatePDFtext(path);
    }
    else if(call.method.equals("getPDFlength")) {
        final String path = call.argument("path");
        getPDFlength(path);
    }
    else 
    {
      result.notImplemented();
    }   
  }

  // Creates a [AsyncTask] to parse the text from the pdf file.
  // This has to be done or else the Main Thread is blocked and user would close the app.
  private void parsePDFtext(String path)
  {
    PdfAsyncTask task = new PdfAsyncTask();
    task.execute(path);
  }
  private void paginatePDFtext(String path)
  {
    PaginatePDFAsyncTask task = new PaginatePDFAsyncTask();
    task.execute(path);
  }
  // Gets the number of pages in PDF document
  private void getPDFlength(String path)
  {
     PDDocument document = null;
          try {
              // Loading the document
                 File renderFile = new File(path);
                 document = PDDocument.load(renderFile);
                  
            } catch(IOException e) {
                Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while loading document to strip", e);
            }finally {
                try {
                    if (document != null) document.close();
                }
                catch (IOException e)
                {
                    Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while closing document", e);
                }
            }
   
     res.success(document.getNumberOfPages());
  }


  // This is called when [PDfAsyncTask] has finished and returns the string.
  static void doneParsing(String parsedText) {
        res.success(parsedText);
    }
  // This is called when [PDfAsyncTask] has finished and returns the string.
  static void donePaginating(ArrayList<String> paginatedText) {
        res.success(paginatedText);
    }
    // This [AsyncTask] runs on another Thread, so that it doesn't block Main Thread
    // [doInBackGround] is used for the parsing and [onPostExecute] is used to return the text back to Main Thread
    private class PdfAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... strings) {

            String parsedText = null;
            PDDocument document = null;
            try {
                // The strings contains all arguments passed to the [AsyncTask], we only pass one argument so we take the first item
                final String path = strings[0];
                File renderFile = new File(path);
                document = PDDocument.load(renderFile);
            } catch(IOException e) {
                Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while loading document", e);
            }

            try {
              // Create stripper that can parse the pdf document.
                PDFTextStripper pdfStripper = new PDFTextStripper();

                // Get the text/strings from the document
                parsedText = pdfStripper.getText(document);
          
            }
            catch (IOException e)
            {
                Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while stripping text", e);
            } finally {
                try {
                    if (document != null) document.close();
                }
                catch (IOException e)
                {
                    Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while closing document", e);
                }
            }
          
            return parsedText;

        }
        // [doInBackground] returns the string to here.
        // Call [doneParsing] to pass the string back to Main Thread
        @Override
        protected void onPostExecute(String parsedText) {
            super.onPostExecute(parsedText);

           doneParsing(parsedText);
        }
    }
    // This [AsyncTask] runs on another Thread, so that it doesn't block Main Thread
    // [doInBackGround] is used for the parsing and [onPostExecute] is used to return the text back to Main Thread
    private class PaginatePDFAsyncTask extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected ArrayList<String> doInBackground(String... strings) {

            ArrayList<String> paginatedText = new ArrayList<String>();
            PDDocument document = null;
            try {
                // The strings contains all arguments passed to the [AsyncTask], we only pass one argument so we take the first item
                final String path = strings[0];
                File renderFile = new File(path);
                document = PDDocument.load(renderFile);
            } catch(IOException e) {
                Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while loading document", e);
            }

            try {
              // Create stripper that can parse the pdf document.
                PDFTextStripper pdfStripper = new PDFTextStripper();
                // Get documentLength
                int documentLength = document.getNumberOfPages();

              //Paginating the text from PDF file
               for(int i = 1; i <= documentLength; i++)
               {
                 // Set page that is gonna be parsed
                   pdfStripper.setStartPage(i);
                   pdfStripper.setEndPage(i);
                   paginatedText.add(pdfStripper.getText(document));
               }
         
            }
            catch (IOException e)
            {
                Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while stripping text", e);
            } finally {
                try {
                    if (document != null) document.close();
                }
                catch (IOException e)
                {
                    Log.e("Flutter-Read-Pdf-Plugin", "Exception thrown while closing document", e);
                }
            }

            return paginatedText;

        }
        // [doInBackground] returns the string to here.
        // Call [doneParsing] to pass the string back to Main Thread
        @Override
        protected void onPostExecute(ArrayList<String> paginatedText) {
            super.onPostExecute(paginatedText);

           donePaginating(paginatedText);
        }
    }
  

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  
}
