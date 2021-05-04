# android-smb-provider
An app to plug SMB shares into Android's system file picker (Storage Access Framework)

## What this is
An app that allows users to add SMB shares and makes documents on those shares available to apps which are using
Android's [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider).

## What this is not
An SMB file manager, video player, or anything else that actually opens files on an SMB share. This app only provides the files to other apps.

## Why?
Android doesn't have native SMB support. Generally, if you want to do something like watch a video on an SMB share, you have to get a video player app which
specifically supports SMB. However, Android's Storage Access Framework can integrate with [custom document providers](https://developer.android.com/guide/topics/providers/create-document-provider),
which I thought would be perfect with SMB. For example, this would allow you to open up a mail app and add an attachment from an SMB share, even if
the mail app doesn't natively support SMB. When I looked for any existing app that was already doing this, I was surprised to only find one
\- https://github.com/google/samba-documents-provider - and it's no longer on the Play Store and hasn't been touched in 4 years.
(Apparently you can still download the APK on some 3rd-party app repos, but eh.) So, I figured I'd try to build a new one myself. If it ends up good enough to
go on the Play Store, then I'm sure there are other people besides me who will appreciate it.
