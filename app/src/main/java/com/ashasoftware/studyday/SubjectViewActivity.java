package com.ashasoftware.studyday;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by tiago on 05/11/15.
 */
public class SubjectViewActivity extends AppCompatActivity implements SubjectView.OnCommandListener {

    private ListView list;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.layout_subject_view_activity );

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setTitle( R.string.words_subjects );
        }

        //Define o evento de clique para o botao flutuante.
        findViewById( R.id.fab ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new AddOrEditSubjectDialog( SubjectViewActivity.this, null ).show();
            }
        } );

        list = (ListView) findViewById( R.id.subject_view_list );

        //Carrega a lista de matérias.
        update();
    }

    private void update() {
        //Carrega a lista de matérias e a exibe.
        list.setAdapter( new SubjectViewAdapter( App.getDatabase().getAllMaterias() ) );
    }

    @Override
    public void onEdit( View v, Materia m ) {
        new AddOrEditSubjectDialog( this, m ).show();
    }

    @Override
    public void onDelete( View v, final Materia m ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Delete: " + m.getNome() );
        builder.setNegativeButton( R.string.words_no, null );
        builder.setPositiveButton( R.string.words_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                App.getDatabase().deleteMateria( m.getCodigo() );
                update();
            }
        } );
        builder.show();
    }

    @Override
    public void onClick( View v, Materia m ) {
        Toast.makeText( getBaseContext(), m.getNome(), Toast.LENGTH_SHORT ).show();
    }

    //"Converte" cada matéria em uma view.
    private class SubjectViewAdapter extends BaseAdapter {

        private List<Materia> materias;

        public SubjectViewAdapter( List<Materia> materias ) {
            this.materias = materias;
        }

        @Override
        public int getCount() {
            return materias.size();
        }

        @Override
        public Object getItem( int position ) {
            return null;
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            Materia m = materias.get( position );

            if( convertView == null ) {
                SubjectView sv = new SubjectView();
                sv.setMateria( m );
                sv.setOnCommandListener( SubjectViewActivity.this );
                sv.setLayoutParams( new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 150 ) );
                return sv;
            }

            return convertView;
        }
    }

    public class AddOrEditSubjectDialog implements SeekBar.OnSeekBarChangeListener {

        private final AlertDialog.Builder builder;
        private final EditText name, teacher;
        private final SeekBar subjectLevel, teacherLevel;
        private final TextView subjectLevelValue, teacherLevelValue;
        private final Materia materia;

        public AddOrEditSubjectDialog( Context context, Materia materia ) {
            this.materia = materia;

            //Cria o construtor da janela de dialogo.
            builder = new AlertDialog.Builder( context );
            //Habilita dois botões.
            builder.setNegativeButton( R.string.words_cancel, null );
            builder.setPositiveButton( R.string.words_ok, ok );
            //Define o titulo da janela.
            builder.setTitle( materia == null ? R.string.words_add_subject : R.string.words_edit_subject );
            //Infla o conteudo da janela e adiciona-o.
            LayoutInflater li = (LayoutInflater) App.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View v = li.inflate( R.layout.layout_add_subject_dialog, null );
            builder.setView( v );

            //Obtem os controles da janela.
            name = (EditText) v.findViewById( R.id.subject_name );
            teacher = (EditText) v.findViewById( R.id.prof_name );
            subjectLevel = (SeekBar) v.findViewById( R.id.subject_level );
            teacherLevel = (SeekBar) v.findViewById( R.id.prof_level );
            subjectLevelValue = (TextView) v.findViewById( R.id.subject_level_value );
            teacherLevelValue = (TextView) v.findViewById( R.id.prof_level_value );
            subjectLevel.setOnSeekBarChangeListener( this );
            teacherLevel.setOnSeekBarChangeListener( this );

            //Está no modo edição. Preenche os campos.
            if( materia != null ) {
                name.setText( materia.getNome() );
                teacher.setText( materia.getProfessor() );
                subjectLevel.setProgress( materia.getDifMateria() );
                teacherLevel.setProgress( materia.getDifProfessor() );
            }
        }

        //Evento chamando quando o usuário clicar no botao OK.
        private final DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {

                //O nome da matéria é requerida.
                if( name.getText().length() == 0 ) {
                    name.requestFocus();
                    Toast.makeText( App.getContext(),
                                    App.getContext().getResources().getText( R.string.words_invalid_field ),
                                    Toast.LENGTH_SHORT ).show();
                    return;
                }

                //Modo adição. Adiciona a matéria ao banco de dados.
                if( materia == null ) {
                    App.getDatabase().addMateria( name.getText().toString(),
                                                  teacher.getText().toString(),
                                                  Color.GREEN,
                                                  teacherLevel.getProgress(),
                                                  subjectLevel.getProgress() );
                } //Modo edição. Edita a máteria e atualiza no banco de dados.
                else {
                    materia.setNome( name.getText().toString() );
                    materia.setProfessor( teacher.getText().toString() );
                    materia.setDifMateria( subjectLevel.getProgress() );
                    materia.setDifProfessor( teacherLevel.getProgress() );
                    materia.setCor( Color.GREEN );
                    App.getDatabase().updateMateria( materia );
                }

                //Recarrega a lista de matérias.
                update();
            }
        };

        //Exibe a janela de dialogo.
        public void show() {
            builder.show();
        }

        @Override
        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {
            if( seekBar == subjectLevel ) {
                subjectLevelValue.setText( String.valueOf( progress ) );
            } else if( seekBar == teacherLevel ) {
                teacherLevelValue.setText( String.valueOf( progress ) );
            }
        }

        @Override
        public void onStartTrackingTouch( SeekBar seekBar ) {

        }

        @Override
        public void onStopTrackingTouch( SeekBar seekBar ) {

        }
    }
}