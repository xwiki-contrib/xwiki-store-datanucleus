public class DataNucleusNamedQueries
{

    String getWikiMacroDocuments = """
      SELECT space, name, author
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
        WHERE objects.contains(obj)
        VARIABLES xwiki.XWiki.WikiMacroClass obj
    """;

    String getTranslationList = """
      SELECT language
        FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument
        WHERE wiki == :wiki
          && fullName == :fullname
          && language != null
    """;

}
