package com.example.yard2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
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

public class MaterialQRGenerator extends AppCompatActivity
{
    EditText productid, productdesc, productgrade;
    Button generate, print;
    ImageView qr;
    Bitmap qrCodeBitmap;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    TextInputLayout productidtil, productdesctil, productgradetil;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_qrgenerator);
        getSupportActionBar().setTitle("Generate Material QR Code");
        productid=findViewById(R.id.productid);
        productdesc=findViewById(R.id.productdesc);
        productgrade=findViewById(R.id.productgrade);
        generate=findViewById(R.id.generatebuttonmat);
        print=findViewById(R.id.printbutton2);
        qr=findViewById(R.id.qrcode);
        progressBar = findViewById(R.id.loadingProgressBarmat);
        frameLayout = findViewById(R.id.framelayout2);

        productidtil=findViewById(R.id.productidnoTextInputLayout);
        productdesctil=findViewById(R.id.productdescTextInputLayout);
        productgradetil=findViewById(R.id.productgradeTextInputLayout);

        generate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(validateInputs())
                {
                    frameLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    qr.setImageBitmap(null);
                    String inputString = productid.getText().toString() + "," + productdesc.getText().toString() + "," + productgrade.getText().toString();
                    generateQRCodeAsync(inputString);
                }
            }
        });

        print.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                File pdfFile=convertBitmapToPdf(qrCodeBitmap,productid.getText().toString());
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
                            qr.setImageBitmap(qrCodeBitmap);
                        else
                            Toast.makeText(MaterialQRGenerator.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    private Bitmap generateQRCode(String inputString)
    {
        QRCodeWriter qrCodeWriter=new QRCodeWriter();
        try
        {
            BitMatrix bitMatrix=qrCodeWriter.encode(inputString, BarcodeFormat.QR_CODE, 1200,1200);
            int width= bitMatrix.getWidth();
            int height= bitMatrix.getHeight();
            Bitmap qrCodeBitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for(int x=0;x<width;x++)
                for(int y=0;y<height;y++)
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(android.R.color.black) : getResources().getColor(android.R.color.white));
            return qrCodeBitmap;
        }
        catch(WriterException e)
        {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
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
    private boolean validateInputs()
    {
        String pidval=productid.getText().toString();
        String producdescval=productdesc.getText().toString();
        String productgradval=productgrade.getText().toString();
        boolean isValid=true;
        if(pidval.isEmpty() || pidval.length()>6 || !checkForSpecialCharacters(pidval))
        {
            productidtil.setError("Invalid Product ID");
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show();
            isValid=false;
        }
        if(producdescval.isEmpty() || producdescval.length()>40 || !checkForSpecialCharacters(producdescval))
        {
            productdesctil.setError("Invalid product description");
            Toast.makeText(this, "Invalid Product Description", Toast.LENGTH_SHORT).show();
            isValid=false;
        }
        if(productgradval.length()>10 || !checkForSpecialCharacters(productgradval))
        {
            productgradetil.setError("Invalid Product grade");
            Toast.makeText(this, "Invalid Product Grade", Toast.LENGTH_SHORT).show();
            isValid=false;
        }
        return isValid;
    }
    private boolean checkForSpecialCharacters(String val)
    {
        for(int x=0;x<val.length();x++)
            if(!(Character.isLetterOrDigit(val.charAt(x)) || Character.isWhitespace(val.charAt(x))))
                return false;
        return true;
    }
}