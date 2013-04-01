/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.AbbuchungControl;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class AbbuchungParam
{
  public final int abbuchungsmodus;

  public final Date stichtag;

  public final int abbuchungsausgabe;

  public final Date vondatum;

  public final String verwendungszweck;

  public final Boolean zusatzbetraege;

  public final Boolean kursteilnehmer;

  public final Boolean kompakteabbuchung;

  public final Boolean dtausprint;

  public final File dtausfile;

  public final String pdffile;

  public final DBService service;

  public Konto konto;

  public AbbuchungParam(AbbuchungControl ac, File dtausfile, String pdffile)
      throws ApplicationException, RemoteException
  {
    abbuchungsmodus = (Integer) ac.getAbbuchungsmodus().getValue();
    stichtag = (Date) ac.getStichtag().getValue();
    Abrechnungsausgabe aa = (Abrechnungsausgabe) ac.getAbbuchungsausgabe()
        .getValue();
    abbuchungsausgabe = aa.getKey();
    vondatum = (Date) ac.getVondatum().getValue();
    verwendungszweck = (String) ac.getZahlungsgrund().getValue();
    zusatzbetraege = (Boolean) ac.getZusatzbetrag().getValue();
    kursteilnehmer = (Boolean) ac.getKursteilnehmer().getValue();
    kompakteabbuchung = (Boolean) ac.getKompakteAbbuchung().getValue();
    dtausprint = (Boolean) ac.getDtausPrint().getValue();
    this.pdffile = pdffile;
    this.dtausfile = dtausfile;

    if (abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS_EINZELBUCHUNGEN
        || abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS_SAMMELBUCHUNG)
    {
      // DB-Service holen
      try
      {
        service = (DBService) Application.getServiceFactory().lookup(
            HBCI.class, "database");
        DBIterator konten = service.createList(Konto.class);
        while (konten.hasNext())
        {
          konto = (Konto) konten.next();
          if (Einstellungen.getEinstellung().getKonto()
              .equals(konto.getKontonummer())
              && Einstellungen.getEinstellung().getBlz().equals(konto.getBLZ()))
          {
            // passendes Konto gefunden
            break;
          }
          else
          {
            konto = null;
          }
        }
        if (konto == null)
        {
          // Kein passendes Konto gefunden. Deshalb Kontoauswahldialog.
          KontoAuswahlDialog d = new KontoAuswahlDialog(
              KontoAuswahlDialog.POSITION_CENTER);
          konto = (Konto) d.open();
          if (konto == null)
          {
            throw new ApplicationException("Bitte w�hlen Sie ein Konto aus");
          }
        }
      }
      catch (OperationCanceledException e)
      {
        throw new ApplicationException("Bitte w�hlen Sie ein Konto aus");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        throw new ApplicationException(
            "Hibiscus-Datenbank kann nicht ge�ffnet werden.");
      }
    }
    else
    {
      service = null;
    }

  }
}
