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

import static org.eclipse.lemminx.XMLAssert.assertSurroundWith;

import java.util.function.Consumer;

import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.extensions.contentmodel.commands.SurroundWithCommand.SurroundWithKind;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.jupiter.api.Test;

/**
 * Tests with {@link SurroundWithCommand} command with Tags
 *
 */
public class SurroundWithTagsCommandTest extends BaseFileTempTest {

	// --------------- Surround with Tags

	@Test
	public void surroundInText() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s|ome te|xt\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<${1:}>ome te</${1:}>$0xt\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundInStartTag() throws Exception {
		String xml = "<fo|o>\r\n" + //
				"	som|e text\r\n" + //
				"</foo>";
		String expected = "<fo<${1:}>o>\r\n" + //
				"	som</${1:}>$0e text\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundElement() throws Exception {
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

	@Test
	public void surroundEmptySelectionInText() throws Exception {
		String xml = "<foo>\r\n" + //
				"	s|ome text\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	s<${1:}>$2</${1:}>$0ome text\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInStartTag() throws Exception {
		String xml = "<f|oo>\r\n" + //
				"	some text\r\n" + //
				"</foo>";
		String expected = "<${1:}><foo>\r\n" + //
				"	some text\r\n" + //
				"</foo></${1:}>$0";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInStartTag2() throws Exception {
		String xml = "<foo>\r\n" + //
				"	<b|ar></bar>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	<${1:}><bar></bar></${1:}>$0\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInEndTag() throws Exception {
		String xml = "<foo>\r\n" + //
				"	some text\r\n" + //
				"</fo|o>";
		String expected = "<${1:}><foo>\r\n" + //
				"	some text\r\n" + //
				"</foo></${1:}>$0";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInEndTag2() throws Exception {
		String xml = "<foo>\r\n" + //
				"	<bar></b|ar>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"	<${1:}><bar></bar></${1:}>$0\r\n" + //
				"</foo>";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInEmptyText() throws Exception {
		String xml = "|";
		String expected = "<${1:}>$2</${1:}>$0";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	@Test
	public void surroundEmptySelectionInEmptyDocumentWithFileAssociation() throws Exception {
		Consumer<XMLLanguageService> configuration = (service) -> {
			service.initializeIfNeeded();
			ContentModelManager cmManager = (ContentModelManager) service.getComponent(ContentModelManager.class);
			cmManager.setRootURI("src/test/resources/xsd/");
			cmManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
		};

		String xml = "|";
		String expected = "<${1|resources|}>$2</${1:resources}>$0";
		assertSurroundWith(xml, SurroundWithKind.tags, true, configuration, "file:///test/resources.xml", expected);
	}

	@Test
	public void surroundEmptySelectionInEmptyDocumentWithTwoSchema() throws Exception {
		String xml = "<?xml-model href=\"relaxng/tei_all.rng\" ?>\r\n" + //
				"<?xml-model href=\"relaxng/simple.rng\" ?>\r\n" + //
				"|";
		String expected = "<?xml-model href=\"relaxng/tei_all.rng\" ?>\r\n" + //
				"<?xml-model href=\"relaxng/simple.rng\" ?>\r\n" + //
				"<${1|TEI,rootelt,teiCorpus|}>$2</${1:TEI}>$0";
		assertSurroundWith(xml, SurroundWithKind.tags, true, expected);
	}

	private static XMLFileAssociation[] createXSDAssociationsNoNamespaceSchemaLocationLike(String baseSystemId) {
		XMLFileAssociation resources = new XMLFileAssociation();
		resources.setPattern("**/*resources*.xml");
		resources.setSystemId(baseSystemId + "resources.xsd");
		return new XMLFileAssociation[] { resources };
	}
}
