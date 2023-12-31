package com.example.yard2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BinQRGenerator extends AppCompatActivity
{
    ImageView qrcode;
    EditText inputText;
    Button generatebutton, savepdfbutton;
    Bitmap qrCodeBitmap;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binqrgenerator);
        getSupportActionBar().setTitle("Generate Bin QR Code");

        qrcode = findViewById(R.id.qrgenerator);
        inputText = findViewById(R.id.inputText);
        generatebutton = findViewById(R.id.generatebutton);
        savepdfbutton = findViewById(R.id.savepdfbutton2);
        progressBar = findViewById(R.id.loadingProgressBar);
        frameLayout = findViewById(R.id.framelayout);
        textInputLayout=findViewById(R.id.binnoTextInputLayout);

        generatebutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(validateInput())
                {
                    frameLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    qrcode.setImageBitmap(null);
                    generateQRCodeAsync(inputText.getText().toString());
                }
            }
        });

        savepdfbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                File pdfFile = convertBitmapToPdf(qrCodeBitmap, inputText.getText().toString());
                printPdf(pdfFile);
            }
        });
    }

    private void generateQRCodeAsync(final String inputString)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                qrCodeBitmap = generateQRCode(inputString);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        progressBar.setVisibility(View.GONE);
                        if (qrCodeBitmap != null)
                            qrcode.setImageBitmap(qrCodeBitmap);
                        else
                            Toast.makeText(BinQRGenerator.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private Bitmap generateQRCode(String inputString)
    {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try
        {
            BitMatrix bitMatrix = qrCodeWriter.encode(inputString, BarcodeFormat.QR_CODE, 1200, 1200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(android.R.color.black) : getResources().getColor(android.R.color.white));
            return qrCodeBitmap;
        }
        catch (WriterException e)
        {
            return null;
        }
    }

    private File convertBitmapToPdf(Bitmap qrCodeBitmap, String filename)
    {
        try
        {
            String filePath = getFilesDir().getPath() + File.separator + filename + ".pdf";
            File pdfFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(pdfFile);

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDocument);

            com.itextpdf.layout.element.Image qrCodeImage = new com.itextpdf.layout.element.Image(ImageDataFactory.create(bitmapToByteArray(qrCodeBitmap)));
            document.add(qrCodeImage);

            document.close();
            outputStream.close();

            return pdfFile;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    private void printPdf(File pdfFile)
    {
        if (pdfFile != null)
        {
            Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);

            Intent printIntent = new Intent(Intent.ACTION_SEND);
            printIntent.setType("application/pdf");
            printIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            printIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try
            {
                startActivity(Intent.createChooser(printIntent, "Print PDF"));
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(this, "No printing app available.", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "PDF file not generated", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean validateInput()
    {
        boolean isValid=true;
        String input=inputText.getText().toString();
        if (input.isEmpty())
        {
            Toast.makeText(this, "Bin number is required", Toast.LENGTH_SHORT).show();
            textInputLayout.setError("Bin number is required");
            isValid = false;
        }
        else if(!checkForSpecialCharacters(input))
        {
            Toast.makeText(this, "Please enter Bin number in correct format", Toast.LENGTH_SHORT).show();
            textInputLayout.setError("Please enter Bin number in correct format");
            isValid = false;
        }
        else if(input.length()>6)
        {
            Toast.makeText(this, "Invalid size", Toast.LENGTH_SHORT).show();
            textInputLayout.setError("Invalid size");
            isValid=false;
        }
        return isValid;
    }
    private boolean checkForSpecialCharacters(String input)
    {
        for(int x=0;x<input.length();x++)
            if(!Character.isLetterOrDigit(input.charAt(x)))
                return false;
        return true;
    }
}