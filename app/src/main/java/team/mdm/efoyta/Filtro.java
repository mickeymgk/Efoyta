package team.mdm.efoyta;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import team.mdm.efoyta.model.Contact;
import team.mdm.efoyta.model.Message;

public class Filtro {

    public static List<Message> getMessages(Context context) {
        List<Message> messages = new ArrayList<Message>();
        final String[] PROJECTION = new String[]{
                "_id",
                "date",
                "message_count",
                "recipient_ids",
                "snippet", // last message
                "read",
                "type"
        };
        Uri URI = Uri.parse("content://mms-sms/conversations?simple=true");
        ContentResolver mResolver = context.getContentResolver();

        Cursor cursor = mResolver.query(URI, PROJECTION, null, null, "date DESC");
        //context.startManagingCursor(cursor);

        cursor.moveToFirst();
        do {

            Message message = new Message();
            message.setThreadId(cursor.getLong(0));
            message.setDate(cursor.getLong(1));
            message.setMsgCount(cursor.getInt(2));
            message.setRecipient(cursor.getString(3));
            message.setSnippet(cursor.getString(4));
            message.setRead(cursor.getInt(5) == 1);


            int recipient_id = cursor.getInt(3);
            Contact contact = getContactByRecipientId(context, recipient_id);
            message.setContact(contact);
            messages.add(message);

            //Log.d("GET_NUMBER", String.valueOf(recipient_id));
            //Toast.makeText(context, String.valueOf(recipient_id), Toast.LENGTH_SHORT).show();

        } while (cursor.moveToNext());

        return messages;
    }

    public static Contact getContactByRecipientId(Context context, long recipientId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor addrCursor = contentResolver.query(Uri.parse("content://mms-sms/canonical-address/" + recipientId), null, null, null, null);
        addrCursor.moveToFirst();
        String number = addrCursor.getString(0); // we got number here
        number = number.replace(" ", "");
        number = number.replace("-", "");
        //Log.d("GET_NUMBER", number);
        Contact c = findContactByNumber(context, number);
        return c;
    }

    public static Contact findContactByNumber(Context context, String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NORMALIZED_NUMBER, ContactsContract.PhoneLookup._ID};

        String name = null;
        String nPhoneNumber = phoneNumber;
        long id = 0;

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {

            if (cursor.moveToFirst()) {
                nPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                id = cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
            }
        }

        Contact contact = new Contact();
        contact.setId(id);
        contact.setName(name);
        contact.setNumber(nPhoneNumber);

        return contact;
    }
}
