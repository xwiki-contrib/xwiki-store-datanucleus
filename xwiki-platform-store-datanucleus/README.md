DataNucleus storage modules
===========================

* xwiki-platform-store-datanucleus-base Depends on DataNucleus, Groovy (for class compiling),
provides PersistableObject storage and PersistableClass storage and loading.

* xwiki-platform-store-datanucleus-cassandra Depends on -base and Cassandra, provides a
Cassandra backend for Datanucleus.

* xwiki-platform-store-datanucleus-documents Depends on -base, -cassandra, and oldcore, provides storage
for oldcode XWikiDocument, BaseObject, XWikiAttachment, XWikiLink, and XWikiLock.
