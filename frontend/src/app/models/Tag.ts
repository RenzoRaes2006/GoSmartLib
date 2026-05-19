export type TagType = 'GENRE' | 'THEMATISCH' | 'DIDACTISCH';

export interface Tag {
  id: number;
  naam: string;
  type: TagType;
}
