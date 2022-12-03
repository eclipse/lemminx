/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.eclipse.lemminx.MockXMLLanguageServer;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.extensions.contentmodel.commands.SurroundWithCommand.SurroundWithKind;
import org.eclipse.lemminx.extensions.contentmodel.commands.SurroundWithCommand.SurroundWithResponse;
import org.eclipse.lemminx.services.format.TextEditUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.Test;

/**
 * Tests with {@link SurroundWithCommand} command
 *
 */
public class SurroundWithCommandTest extends BaseFileTempTest {

	// --------------- Surround with Tags

	@Test
	public void surroundTextWithTags() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s|ome te|xt\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<${1:}>ome te</${1:}>$0xt\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundTextWithTagsAndEmptySelection() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s||ome text\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<${1:}>$2</${1:}>$0ome text\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundElementWithTags() throws Exception {
		String xml = "<foo>\r\n" + //
				"	|<bar></bar>|\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	<${1:}><bar></bar></${1:}>$0\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundElementWithTagsWithRelaxNG() throws Exception {
		String xml = "<?xml-model href=\"relaxng/tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"	<teiHeader>\r\n" + "		<fileDesc>\r\n" + //
				"			<titleStmt>\r\n" + //
				"				<title>so|me con|tent</title>\r\n" + //
				"			</titleStmt>\r\n" + //
				"		</fileDesc>\r\n" + //
				"	</teiHeader>\r\n" + //
				"</TEI>";
		String expected = "<?xml-model href=\"relaxng/tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"	<teiHeader>\r\n" + //
				"		<fileDesc>\r\n" + //
				"			<titleStmt>\r\n" + //
				"				<title>so<${1|abbr,add,addName,addSpan,address,affiliation,alt,altGrp,am,anchor,app,att,bibl,biblFull,biblStruct,binaryObject,bloc,c,caesura,camera,caption,castList,catchwords,cb,certainty,choice,cit,cl,classSpec,climate,code,constraintSpec,corr,country,damage,damageSpan,dataSpec,date,del,delSpan,depth,desc,dim,dimensions,distinct,district,eg,egXML,elementSpec,ellipsis,email,emph,ex,expan,fLib,figure,floatingText,foreign,forename,formula,fs,fvLib,fw,g,gap,gb,genName,geo,geogFeat,geogName,gi,gloss,graphic,handShift,height,heraldry,hi,ident,idno,incident,index,interp,interpGrp,join,joinGrp,kinesic,l,label,lang,lb,lg,link,linkGrp,list,listApp,listBibl,listEvent,listNym,listObject,listOrg,listPerson,listPlace,listRef,listRelation,listTranspose,listWit,location,locus,locusGrp,m,macroSpec,material,measure,measureGrp,media,mentioned,metamark,milestone,mod,moduleSpec,move,msDesc,name,nameLink,notatedMusic,note,noteGrp,num,oRef,objectName,objectType,offset,orgName,orig,origDate,origPlace,outputRendition,pRef,pause,pb,pc,persName,persPronouns,phr,placeName,population,precision,ptr,q,quote,redo,ref,reg,region,respons,restore,retrace,rhyme,roleName,rs,ruby,s,said,secFol,secl,seg,settlement,shift,sic,signatures,soCalled,sound,space,span,spanGrp,specDesc,specGrp,specGrpRef,specList,stage,stamp,state,subst,substJoin,supplied,surname,surplus,table,tag,tech,term,terrain,time,timeline,title,trait,unclear,undo,unit,val,view,vocal,w,watermark,width,witDetail,writing|}>me con</${1:abbr}>$0tent</title>\r\n"
				+ //
				"			</titleStmt>\r\n" + //
				"		</fileDesc>\r\n" + //
				"	</teiHeader>\r\n" + //
				"</TEI>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	// --------------- Surround with Comments

	@Test
	public void surroundTextWithComments() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s|ome te|xt\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<!--ome te-->$0xt\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.comments, true, expected);
	}

	// --------------- Surround with CDATA

	@Test
	public void surroundTextWithCDATA() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s|ome te|xt\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<![CDATA[ome te]]>$0xt\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.cdata, true, expected);
	}

	private static void assertSurroundWith(String xml, SurroundWithKind kind, boolean snippetsSupported,
			String expected) throws BadLocationException, InterruptedException, ExecutionException {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		int rangeStart = xml.indexOf('|');
		int rangeEnd = xml.lastIndexOf('|');
		// remove '|'
		xml = xml.substring(0, rangeStart) + xml.substring(rangeStart + 1, rangeEnd)
				+ xml.substring(rangeEnd + 1);
		TextDocument document = new TextDocument(xml, "");
		Position startPos = document.positionAt(rangeStart);
		Position endPos = document.positionAt(rangeEnd - 1);
		Range selection = new Range(startPos, endPos);

		TextDocumentIdentifier xmlIdentifier = languageServer.didOpen("src/test/resources/test.xml", xml);

		// Execute surround with tags command
		SurroundWithResponse response = (SurroundWithResponse) languageServer
				.executeCommand(SurroundWithCommand.COMMAND_ID, xmlIdentifier, selection, kind.name(),
						snippetsSupported)
				.get();

		String actual = TextEditUtils.applyEdits(document, Arrays.asList(response.getStart(), response.getEnd()));
		assertEquals(expected, actual);
	}
}
