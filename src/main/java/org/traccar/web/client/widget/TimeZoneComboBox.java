/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.TimeZoneInfo;
import com.google.gwt.i18n.client.constants.TimeZoneConstants;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeZoneComboBox extends ComboBox<TimeZoneInfo> {
    public TimeZoneComboBox() {
        super(new ListStore<>(new ModelKeyProvider<TimeZoneInfo>() {
            @Override
            public String getKey(TimeZoneInfo item) {
                return item.getID();
            }
        }), new LabelProvider<TimeZoneInfo>() {
            @Override
            public String getLabel(TimeZoneInfo item) {
                int hours = item.getStandardOffset() / 60;
                int minutes = item.getStandardOffset() - hours * 60;
                return "(GMT" + (hours <= 0 ? "" : "+") +
                        (hours == 0 ? "" : hours) +
                        (minutes == 0 ? ":00" : (":" + Math.abs(minutes))) +
                        ") " + item.getID() + " (" + item.getNames().get(0) + ")";
            }
        });

        getStore().addAll(getAllTimeZones());
        setForceSelection(true);
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
    }

    private static List<TimeZoneInfo> getAllTimeZones() {
        List<TimeZoneInfo> result = new ArrayList<>();
        TimeZoneConstants timeZoneConstants = GWT.create(TimeZoneConstants.class);

        for (String timeZoneString : new String[] {
                timeZoneConstants.africaAbidjan(),

                timeZoneConstants.africaAccra(),

                timeZoneConstants.africaAddisAbaba(),

                timeZoneConstants.africaAlgiers(),

                timeZoneConstants.africaAsmera(),

                timeZoneConstants.africaBamako(),

                timeZoneConstants.africaBangui(),

                timeZoneConstants.africaBanjul(),

                timeZoneConstants.africaBissau(),

                timeZoneConstants.africaBlantyre(),

                timeZoneConstants.africaBrazzaville(),

                timeZoneConstants.africaBujumbura(),

                timeZoneConstants.africaCairo(),

                timeZoneConstants.africaCasablanca(),

                timeZoneConstants.africaCeuta(),

                timeZoneConstants.africaConakry(),

                timeZoneConstants.africaDakar(),

                timeZoneConstants.africaDaresSalaam(),

                timeZoneConstants.africaDjibouti(),

                timeZoneConstants.africaDouala(),

                timeZoneConstants.africaElAaiun(),

                timeZoneConstants.africaFreetown(),

                timeZoneConstants.africaGaborone(),

                timeZoneConstants.africaHarare(),

                timeZoneConstants.africaJohannesburg(),

                timeZoneConstants.africaKampala(),

                timeZoneConstants.africaKhartoum(),

                timeZoneConstants.africaKigali(),

                timeZoneConstants.africaKinshasa(),

                timeZoneConstants.africaLagos(),

                timeZoneConstants.africaLibreville(),

                timeZoneConstants.africaLome(),

                timeZoneConstants.africaLuanda(),

                timeZoneConstants.africaLubumbashi(),

                timeZoneConstants.africaLusaka(),

                timeZoneConstants.africaMalabo(),

                timeZoneConstants.africaMaputo(),

                timeZoneConstants.africaMaseru(),

                timeZoneConstants.africaMbabane(),

                timeZoneConstants.africaMogadishu(),

                timeZoneConstants.africaMonrovia(),

                timeZoneConstants.africaNairobi(),

                timeZoneConstants.africaNdjamena(),

                timeZoneConstants.africaNiamey(),

                timeZoneConstants.africaNouakchott(),

                timeZoneConstants.africaOuagadougou(),

                timeZoneConstants.africaPortoNovo(),

                timeZoneConstants.africaSaoTome(),

                timeZoneConstants.africaTripoli(),

                timeZoneConstants.africaTunis(),

                timeZoneConstants.africaWindhoek(),

                timeZoneConstants.americaAdak(),

                timeZoneConstants.americaAnchorage(),

                timeZoneConstants.americaAnguilla(),

                timeZoneConstants.americaAntigua(),

                timeZoneConstants.americaAraguaina(),

                timeZoneConstants.americaArgentinaLaRioja(),

                timeZoneConstants.americaArgentinaRioGallegos(),

                timeZoneConstants.americaArgentinaSalta(),

                timeZoneConstants.americaArgentinaSanJuan(),

                timeZoneConstants.americaArgentinaSanLuis(),

                timeZoneConstants.americaArgentinaTucuman(),

                timeZoneConstants.americaArgentinaUshuaia(),

                timeZoneConstants.americaAruba(),

                timeZoneConstants.americaAsuncion(),

                timeZoneConstants.americaBahia(),

                timeZoneConstants.americaBahiaBanderas(),

                timeZoneConstants.americaBarbados(),

                timeZoneConstants.americaBelem(),

                timeZoneConstants.americaBelize(),

                timeZoneConstants.americaBlancSablon(),

                timeZoneConstants.americaBoaVista(),

                timeZoneConstants.americaBogota(),

                timeZoneConstants.americaBoise(),

                timeZoneConstants.americaBuenosAires(),

                timeZoneConstants.americaCambridgeBay(),

                timeZoneConstants.americaCampoGrande(),

                timeZoneConstants.americaCancun(),

                timeZoneConstants.americaCaracas(),

                timeZoneConstants.americaCatamarca(),

                timeZoneConstants.americaCayenne(),

                timeZoneConstants.americaCayman(),

                timeZoneConstants.americaChicago(),

                timeZoneConstants.americaChihuahua(),

                timeZoneConstants.americaCoralHarbour(),

                timeZoneConstants.americaCordoba(),

                timeZoneConstants.americaCostaRica(),

                timeZoneConstants.americaCreston(),

                timeZoneConstants.americaCuiaba(),

                timeZoneConstants.americaCuracao(),

                timeZoneConstants.americaDanmarkshavn(),

                timeZoneConstants.americaDawson(),

                timeZoneConstants.americaDawsonCreek(),

                timeZoneConstants.americaDenver(),

                timeZoneConstants.americaDetroit(),

                timeZoneConstants.americaDominica(),

                timeZoneConstants.americaEdmonton(),

                timeZoneConstants.americaEirunepe(),

                timeZoneConstants.americaElSalvador(),

                timeZoneConstants.americaFortaleza(),

                timeZoneConstants.americaGlaceBay(),

                timeZoneConstants.americaGodthab(),

                timeZoneConstants.americaGooseBay(),

                timeZoneConstants.americaGrandTurk(),

                timeZoneConstants.americaGrenada(),

                timeZoneConstants.americaGuadeloupe(),

                timeZoneConstants.americaGuatemala(),

                timeZoneConstants.americaGuayaquil(),

                timeZoneConstants.americaGuyana(),

                timeZoneConstants.americaHalifax(),

                timeZoneConstants.americaHavana(),

                timeZoneConstants.americaHermosillo(),

                timeZoneConstants.americaIndianaKnox(),

                timeZoneConstants.americaIndianaMarengo(),

                timeZoneConstants.americaIndianaPetersburg(),

                timeZoneConstants.americaIndianapolis(),

                timeZoneConstants.americaIndianaTellCity(),

                timeZoneConstants.americaIndianaVevay(),

                timeZoneConstants.americaIndianaVincennes(),

                timeZoneConstants.americaIndianaWinamac(),

                timeZoneConstants.americaInuvik(),

                timeZoneConstants.americaIqaluit(),

                timeZoneConstants.americaJamaica(),

                timeZoneConstants.americaJujuy(),

                timeZoneConstants.americaJuneau(),

                timeZoneConstants.americaKentuckyMonticello(),

                timeZoneConstants.americaKralendijk(),

                timeZoneConstants.americaLaPaz(),

                timeZoneConstants.americaLima(),

                timeZoneConstants.americaLosAngeles(),

                timeZoneConstants.americaLouisville(),

                timeZoneConstants.americaLowerPrinces(),

                timeZoneConstants.americaMaceio(),

                timeZoneConstants.americaManagua(),

                timeZoneConstants.americaManaus(),

                timeZoneConstants.americaMarigot(),

                timeZoneConstants.americaMartinique(),

                timeZoneConstants.americaMatamoros(),

                timeZoneConstants.americaMazatlan(),

                timeZoneConstants.americaMendoza(),

                timeZoneConstants.americaMenominee(),

                timeZoneConstants.americaMerida(),

                timeZoneConstants.americaMetlakatla(),

                timeZoneConstants.americaMexicoCity(),

                timeZoneConstants.americaMiquelon(),

                timeZoneConstants.americaMoncton(),

                timeZoneConstants.americaMonterrey(),

                timeZoneConstants.americaMontevideo(),

                timeZoneConstants.americaMontserrat(),

                timeZoneConstants.americaNassau(),

                timeZoneConstants.americaNewYork(),

                timeZoneConstants.americaNipigon(),

                timeZoneConstants.americaNome(),

                timeZoneConstants.americaNoronha(),

                timeZoneConstants.americaNorthDakotaBeulah(),

                timeZoneConstants.americaNorthDakotaCenter(),

                timeZoneConstants.americaNorthDakotaNewSalem(),

                timeZoneConstants.americaOjinaga(),

                timeZoneConstants.americaPanama(),

                timeZoneConstants.americaPangnirtung(),

                timeZoneConstants.americaParamaribo(),

                timeZoneConstants.americaPhoenix(),

                timeZoneConstants.americaPortauPrince(),

                timeZoneConstants.americaPortofSpain(),

                timeZoneConstants.americaPortoVelho(),

                timeZoneConstants.americaPuertoRico(),

                timeZoneConstants.americaRainyRiver(),

                timeZoneConstants.americaRankinInlet(),

                timeZoneConstants.americaRecife(),

                timeZoneConstants.americaRegina(),

                timeZoneConstants.americaResolute(),

                timeZoneConstants.americaRioBranco(),

                timeZoneConstants.americaSantaIsabel(),

                timeZoneConstants.americaSantarem(),

                timeZoneConstants.americaSantiago(),

                timeZoneConstants.americaSantoDomingo(),

                timeZoneConstants.americaSaoPaulo(),

                timeZoneConstants.americaScoresbysund(),

                timeZoneConstants.americaSitka(),

                timeZoneConstants.americaStBarthelemy(),

                timeZoneConstants.americaStJohns(),

                timeZoneConstants.americaStKitts(),

                timeZoneConstants.americaStLucia(),

                timeZoneConstants.americaStThomas(),

                timeZoneConstants.americaStVincent(),

                timeZoneConstants.americaSwiftCurrent(),

                timeZoneConstants.americaTegucigalpa(),

                timeZoneConstants.americaThule(),

                timeZoneConstants.americaThunderBay(),

                timeZoneConstants.americaTijuana(),

                timeZoneConstants.americaToronto(),

                timeZoneConstants.americaTortola(),

                timeZoneConstants.americaVancouver(),

                timeZoneConstants.americaWhitehorse(),

                timeZoneConstants.americaWinnipeg(),

                timeZoneConstants.americaYakutat(),

                timeZoneConstants.americaYellowknife(),

                timeZoneConstants.antarcticaCasey(),

                timeZoneConstants.antarcticaDavis(),

                timeZoneConstants.antarcticaDumontDUrville(),

                timeZoneConstants.antarcticaMacquarie(),

                timeZoneConstants.antarcticaMawson(),

                timeZoneConstants.antarcticaMcMurdo(),

                timeZoneConstants.antarcticaPalmer(),

                timeZoneConstants.antarcticaRothera(),

                timeZoneConstants.antarcticaSyowa(),

                timeZoneConstants.antarcticaVostok(),

                timeZoneConstants.arcticLongyearbyen(),

                timeZoneConstants.asiaAden(),

                timeZoneConstants.asiaAlmaty(),

                timeZoneConstants.asiaAmman(),

                timeZoneConstants.asiaAnadyr(),

                timeZoneConstants.asiaAqtau(),

                timeZoneConstants.asiaAqtobe(),

                timeZoneConstants.asiaAshgabat(),

                timeZoneConstants.asiaBaghdad(),

                timeZoneConstants.asiaBahrain(),

                timeZoneConstants.asiaBaku(),

                timeZoneConstants.asiaBangkok(),

                timeZoneConstants.asiaBeirut(),

                timeZoneConstants.asiaBishkek(),

                timeZoneConstants.asiaBrunei(),

                timeZoneConstants.asiaCalcutta(),

                timeZoneConstants.asiaChoibalsan(),

                timeZoneConstants.asiaChongqing(),

                timeZoneConstants.asiaColombo(),

                timeZoneConstants.asiaDamascus(),

                timeZoneConstants.asiaDhaka(),

                timeZoneConstants.asiaDili(),

                timeZoneConstants.asiaDubai(),

                timeZoneConstants.asiaDushanbe(),

                timeZoneConstants.asiaGaza(),

                timeZoneConstants.asiaHarbin(),

                timeZoneConstants.asiaHongKong(),

                timeZoneConstants.asiaHovd(),

                timeZoneConstants.asiaIrkutsk(),

                timeZoneConstants.asiaJakarta(),

                timeZoneConstants.asiaJayapura(),

                timeZoneConstants.asiaJerusalem(),

                timeZoneConstants.asiaKabul(),

                timeZoneConstants.asiaKamchatka(),

                timeZoneConstants.asiaKarachi(),

                timeZoneConstants.asiaKashgar(),

                timeZoneConstants.asiaKatmandu(),

                timeZoneConstants.asiaKrasnoyarsk(),

                timeZoneConstants.asiaKualaLumpur(),

                timeZoneConstants.asiaKuching(),

                timeZoneConstants.asiaKuwait(),

                timeZoneConstants.asiaMacau(),

                timeZoneConstants.asiaMagadan(),

                timeZoneConstants.asiaMakassar(),

                timeZoneConstants.asiaManila(),

                timeZoneConstants.asiaMuscat(),

                timeZoneConstants.asiaNicosia(),

                timeZoneConstants.asiaNovokuznetsk(),

                timeZoneConstants.asiaNovosibirsk(),

                timeZoneConstants.asiaOmsk(),

                timeZoneConstants.asiaOral(),

                timeZoneConstants.asiaPhnomPenh(),

                timeZoneConstants.asiaPontianak(),

                timeZoneConstants.asiaPyongyang(),

                timeZoneConstants.asiaQatar(),

                timeZoneConstants.asiaQyzylorda(),

                timeZoneConstants.asiaRangoon(),

                timeZoneConstants.asiaRiyadh(),

                timeZoneConstants.asiaSaigon(),

                timeZoneConstants.asiaSakhalin(),

                timeZoneConstants.asiaSamarkand(),

                timeZoneConstants.asiaSeoul(),

                timeZoneConstants.asiaShanghai(),

                timeZoneConstants.asiaSingapore(),

                timeZoneConstants.asiaTaipei(),

                timeZoneConstants.asiaTashkent(),

                timeZoneConstants.asiaTbilisi(),

                timeZoneConstants.asiaTehran(),

                timeZoneConstants.asiaThimphu(),

                timeZoneConstants.asiaTokyo(),

                timeZoneConstants.asiaUlaanbaatar(),

                timeZoneConstants.asiaUrumqi(),

                timeZoneConstants.asiaVientiane(),

                timeZoneConstants.asiaVladivostok(),

                timeZoneConstants.asiaYakutsk(),

                timeZoneConstants.asiaYekaterinburg(),

                timeZoneConstants.asiaYerevan(),

                timeZoneConstants.atlanticAzores(),

                timeZoneConstants.atlanticBermuda(),

                timeZoneConstants.atlanticCanary(),

                timeZoneConstants.atlanticCapeVerde(),

                timeZoneConstants.atlanticFaeroe(),

                timeZoneConstants.atlanticMadeira(),

                timeZoneConstants.atlanticReykjavik(),

                timeZoneConstants.atlanticSouthGeorgia(),

                timeZoneConstants.atlanticStanley(),

                timeZoneConstants.atlanticStHelena(),

                timeZoneConstants.australiaAdelaide(),

                timeZoneConstants.australiaBrisbane(),

                timeZoneConstants.australiaBrokenHill(),

                timeZoneConstants.australiaCurrie(),

                timeZoneConstants.australiaDarwin(),

                timeZoneConstants.australiaEucla(),

                timeZoneConstants.australiaHobart(),

                timeZoneConstants.australiaLindeman(),

                timeZoneConstants.australiaLordHowe(),

                timeZoneConstants.australiaMelbourne(),

                timeZoneConstants.australiaPerth(),

                timeZoneConstants.australiaSydney(),

                timeZoneConstants.cST6CDT(),

                timeZoneConstants.eST5EDT(),

                timeZoneConstants.europeAmsterdam(),

                timeZoneConstants.europeAndorra(),

                timeZoneConstants.europeAthens(),

                timeZoneConstants.europeBelgrade(),

                timeZoneConstants.europeBerlin(),

                timeZoneConstants.europeBratislava(),

                timeZoneConstants.europeBrussels(),

                timeZoneConstants.europeBucharest(),

                timeZoneConstants.europeBudapest(),

                timeZoneConstants.europeChisinau(),

                timeZoneConstants.europeCopenhagen(),

                timeZoneConstants.europeDublin(),

                timeZoneConstants.europeGibraltar(),

                timeZoneConstants.europeGuernsey(),

                timeZoneConstants.europeHelsinki(),

                timeZoneConstants.europeIsleofMan(),

                timeZoneConstants.europeIstanbul(),

                timeZoneConstants.europeJersey(),

                timeZoneConstants.europeKaliningrad(),

                timeZoneConstants.europeKiev(),

                timeZoneConstants.europeLisbon(),

                timeZoneConstants.europeLjubljana(),

                timeZoneConstants.europeLondon(),

                timeZoneConstants.europeLuxembourg(),

                timeZoneConstants.europeMadrid(),

                timeZoneConstants.europeMalta(),

                timeZoneConstants.europeMariehamn(),

                timeZoneConstants.europeMinsk(),

                timeZoneConstants.europeMonaco(),

                timeZoneConstants.europeMoscow(),

                timeZoneConstants.europeOslo(),

                timeZoneConstants.europeParis(),

                timeZoneConstants.europePodgorica(),

                timeZoneConstants.europePrague(),

                timeZoneConstants.europeRiga(),

                timeZoneConstants.europeRome(),

                timeZoneConstants.europeSamara(),

                timeZoneConstants.europeSanMarino(),

                timeZoneConstants.europeSarajevo(),

                timeZoneConstants.europeSimferopol(),

                timeZoneConstants.europeSkopje(),

                timeZoneConstants.europeSofia(),

                timeZoneConstants.europeStockholm(),

                timeZoneConstants.europeTallinn(),

                timeZoneConstants.europeTirane(),

                timeZoneConstants.europeUzhgorod(),

                timeZoneConstants.europeVaduz(),

                timeZoneConstants.europeVatican(),

                timeZoneConstants.europeVienna(),

                timeZoneConstants.europeVilnius(),

                timeZoneConstants.europeVolgograd(),

                timeZoneConstants.europeWarsaw(),

                timeZoneConstants.europeZagreb(),

                timeZoneConstants.europeZaporozhye(),

                timeZoneConstants.europeZurich(),

                timeZoneConstants.indianAntananarivo(),

                timeZoneConstants.indianChagos(),

                timeZoneConstants.indianChristmas(),

                timeZoneConstants.indianCocos(),

                timeZoneConstants.indianComoro(),

                timeZoneConstants.indianKerguelen(),

                timeZoneConstants.indianMahe(),

                timeZoneConstants.indianMaldives(),

                timeZoneConstants.indianMauritius(),

                timeZoneConstants.indianMayotte(),

                timeZoneConstants.indianReunion(),

                timeZoneConstants.mST7MDT(),

                timeZoneConstants.pacificApia(),

                timeZoneConstants.pacificAuckland(),

                timeZoneConstants.pacificChatham(),

                timeZoneConstants.pacificEaster(),

                timeZoneConstants.pacificEfate(),

                timeZoneConstants.pacificEnderbury(),

                timeZoneConstants.pacificFakaofo(),

                timeZoneConstants.pacificFiji(),

                timeZoneConstants.pacificFunafuti(),

                timeZoneConstants.pacificGalapagos(),

                timeZoneConstants.pacificGambier(),

                timeZoneConstants.pacificGuadalcanal(),

                timeZoneConstants.pacificGuam(),

                timeZoneConstants.pacificHonolulu(),

                timeZoneConstants.pacificJohnston(),

                timeZoneConstants.pacificKiritimati(),

                timeZoneConstants.pacificKosrae(),

                timeZoneConstants.pacificKwajalein(),

                timeZoneConstants.pacificMajuro(),

                timeZoneConstants.pacificMarquesas(),

                timeZoneConstants.pacificMidway(),

                timeZoneConstants.pacificNauru(),

                timeZoneConstants.pacificNiue(),

                timeZoneConstants.pacificNorfolk(),

                timeZoneConstants.pacificNoumea(),

                timeZoneConstants.pacificPagoPago(),

                timeZoneConstants.pacificPalau(),

                timeZoneConstants.pacificPitcairn(),

                timeZoneConstants.pacificPonape(),

                timeZoneConstants.pacificPortMoresby(),

                timeZoneConstants.pacificRarotonga(),

                timeZoneConstants.pacificSaipan(),

                timeZoneConstants.pacificTahiti(),

                timeZoneConstants.pacificTarawa(),

                timeZoneConstants.pacificTongatapu(),

                timeZoneConstants.pacificTruk(),

                timeZoneConstants.pacificWake(),

                timeZoneConstants.pacificWallis(),

                timeZoneConstants.pST8PDT(),
        }) {
            result.add(TimeZoneInfo.buildTimeZoneData(timeZoneString));
        }

        Collections.sort(result, new Comparator<TimeZoneInfo>() {
            @Override
            public int compare(TimeZoneInfo o1, TimeZoneInfo o2) {
                return o1.getStandardOffset() - o2.getStandardOffset();
            }
        });

        return result;
    }

    public static TimeZoneInfo getByID(String id) {
        if (id == null) {
            return null;
        }
        for (TimeZoneInfo zoneInfo : getAllTimeZones()) {
            if (zoneInfo.getID().equals(id)) {
                return zoneInfo;
            }
        }
        return null;
    }

    public static TimeZoneInfo getByOffset(int offset) {
        for (TimeZoneInfo zoneInfo : getAllTimeZones()) {
            if (zoneInfo.getStandardOffset() == offset) {
                return zoneInfo;
            }
        }
        return null;
    }
}
