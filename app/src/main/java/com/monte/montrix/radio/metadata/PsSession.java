package com.monte.montrix.radio.metadata;
import java.util.*;
/** PS-only cycle decoder. Station aliases are independent identity evidence, not programme text. */
public final class PsSession {
 private int frequency; private long epoch; private String last="", confirmed=""; private long lastConfirmed; private int repeats; private boolean anchored;
 private final List<String> cycle=new ArrayList<>(); private final ArrayDeque<String> runs=new ArrayDeque<>(); private final ArrayDeque<String> votes=new ArrayDeque<>(); private final Set<String> aliases=new HashSet<>();
 public synchronized long tune(int khz){if(khz<87500||khz>108000)throw new IllegalArgumentException("FM frequency");if(khz!=frequency){reset();frequency=khz;}return epoch;}
 private static String norm(String s){return s.trim().replaceAll("[ -]+$","").toUpperCase(Locale.ROOT);}
 public synchronized void station(String name){aliases.clear();if(name!=null&&!name.equalsIgnoreCase("FM radio")){aliases.add(norm(name));for(String part:name.split("\\s+"))if(part.matches("[A-Z]{4}"))aliases.add(part); } }
 private boolean marker(String s){return aliases.contains(norm(s))||norm(s).equals(String.format(Locale.US,"%.1f",frequency/1000.0));}
 public synchronized boolean accept(long generation,String raw){return acceptAt(generation,raw,System.nanoTime()/1000000);}
 public synchronized boolean acceptAt(long generation,String raw,long ms){if(generation!=epoch||frequency==0||raw==null||raw.length()!=8)return false;for(int i=0;i<8;i++)if(raw.charAt(i)<32||raw.charAt(i)>126)return false;String s=raw.trim();if(s.isEmpty())return false;if(raw.equals(last)){repeats++;return true;}last=raw;repeats=1;runs.add(raw);while(runs.size()>48)runs.removeFirst();if(marker(s)){if(anchored&&!cycle.isEmpty())finish(ms);cycle.clear();anchored=true;}else if(anchored){cycle.add(s);if(cycle.size()>16){cycle.clear();anchored=false;}}return true;}
 private void finish(long ms){if(cycle.size()<2)return;String out="";for(String s:cycle){if(out.isEmpty()){out=s;continue;}int n=Math.min(out.length(),s.length());while(n>=4&&!out.regionMatches(true,out.length()-n,s,0,n))n--;out+=n>=4?s.substring(n):" "+s;}out=out.replaceAll("\\s+"," ").trim();if(out.length()<10||out.length()>100)return;votes.add(out);while(votes.size()>4)votes.removeFirst();int count=0;for(String v:votes)if(v.equals(out))count++;if(count>=2){confirmed=out;lastConfirmed=ms;}}
 public synchronized String confirmedAt(long ms){return ms-lastConfirmed>45000?"":confirmed;}
 public synchronized String confirmedCandidate(){return confirmedAt(System.nanoTime()/1000000);}
 public synchronized String latest(){return last;} public synchronized String stablePs(){return repeats>=3?last.trim():"";} public synchronized List<String> observations(){return new ArrayList<>(runs);} public synchronized String overlapCandidate(){return confirmedCandidate();}
 public synchronized void reset(){frequency=0;epoch++;last="";confirmed="";lastConfirmed=0;repeats=0;anchored=false;cycle.clear();runs.clear();votes.clear();aliases.clear();} public synchronized long epoch(){return epoch;}
}
